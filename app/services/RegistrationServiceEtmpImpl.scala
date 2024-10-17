/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import config.AppConfig
import connectors.{EnrolmentsConnector, GetVatInfoConnector, RegistrationConnector}
import controllers.actions.{AuthorisedMandatoryRegistrationRequest, AuthorisedMandatoryVrnRequest}
import models._
import models.amend.EtmpAmendRegistrationRequest
import models.audit.{EtmpAmendRegistrationAuditModel, EtmpDisplayRegistrationAuditModel, EtmpRegistrationAuditModel, EtmpRegistrationAuditType, SubmissionResult}
import models.core.{EisDisplayErrorResponse, MatchType}
import models.enrolments.EtmpEnrolmentErrorResponse
import models.etmp._
import models.repository.{AmendResult, InsertResult}
import models.repository.AmendResult.AmendSucceeded
import models.repository.InsertResult.{AlreadyExists, InsertSucceeded}
import models.requests.{AmendRegistrationRequest, RegistrationRequest}
import play.api.http.Status.NO_CONTENT
import repositories.{CachedRegistrationRepository, RegistrationStatusRepository}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationServiceEtmpImpl @Inject()(
                                             registrationConnector: RegistrationConnector,
                                             enrolmentsConnector: EnrolmentsConnector,
                                             getVatInfoConnector: GetVatInfoConnector,
                                             registrationStatusRepository: RegistrationStatusRepository,
                                             cachedRegistrationRepository: CachedRegistrationRepository,
                                             retryService: RetryService,
                                             appConfig: AppConfig,
                                             auditService: AuditService,
                                             coreValidationService: CoreValidationService
                                           )(implicit ec: ExecutionContext) extends RegistrationService {

  def createRegistration(registrationRequest: RegistrationRequest)
                        (implicit hc: HeaderCarrier, request: AuthorisedMandatoryVrnRequest[_]): Future[InsertResult] = {
    val etmpRegistrationRequest = EtmpRegistrationRequest.fromRegistrationRequest(registrationRequest, EtmpMessageType.OSSSubscriptionCreate)
    val creationResponse = registrationConnector.create(etmpRegistrationRequest).flatMap {
      case Right(response) =>
        auditService.audit(EtmpRegistrationAuditModel.build(EtmpRegistrationAuditType.CreateRegistration, etmpRegistrationRequest, Some(response), None, None, SubmissionResult.Success))
        (for {
          _ <- registrationStatusRepository.delete(response.formBundleNumber)
          _ <- registrationStatusRepository.insert(RegistrationStatus(subscriptionId = response.formBundleNumber,
            status = EtmpRegistrationStatus.Pending))
          enrolmentResponse <- enrolmentsConnector.confirmEnrolment(response.formBundleNumber)
        } yield {
          enrolmentResponse.status match {
            case NO_CONTENT =>
              retryService.getEtmpRegistrationStatus(appConfig.maxRetryCount, appConfig.delay, response.formBundleNumber).flatMap {
                case EtmpRegistrationStatus.Success =>
                  logger.info("Enrolment succeeded")
                  Future.successful(InsertSucceeded)
                case _ =>
                  logger.error(s"Failed to add enrolment")
                  registrationStatusRepository.set(RegistrationStatus(subscriptionId = response.formBundleNumber, status = EtmpRegistrationStatus.Error))
                  throw EtmpException("Failed to add enrolment")
              }
            case status =>
              logger.error(s"Failed to add enrolment - $status with body ${enrolmentResponse.body}")
              throw EtmpException(s"Failed to add enrolment - ${enrolmentResponse.body}")
          }
        }).flatten
      case Left(EtmpEnrolmentError(EtmpEnrolmentErrorResponse.alreadyActiveSubscriptionErrorCode, _)) =>
        logger.warn("Enrolment already existed")
        auditService.audit(EtmpRegistrationAuditModel.build(EtmpRegistrationAuditType.CreateRegistration, etmpRegistrationRequest, None, None, None, SubmissionResult.Duplicate))
        Future.successful(AlreadyExists)
      case Left(error) =>
        logger.error(s"There was an error creating Registration enrolment from ETMP: $error")
        auditService.audit(EtmpRegistrationAuditModel.build(EtmpRegistrationAuditType.CreateRegistration, etmpRegistrationRequest, None, None, Some(error.body), SubmissionResult.Failure))
        throw EtmpException(s"There was an error creating Registration enrolment from ETMP: ${error.body}")
    }

    if (appConfig.registrationCacheEnabled) {
      cachedRegistrationRepository.clear(request.userId)
    }
    creationResponse
  }

  def get(vrn: Vrn)(implicit headerCarrier: HeaderCarrier, request: AuthorisedMandatoryVrnRequest[_]): Future[Option[Registration]] = {
    val auditBlock = (etmpRegistration: EtmpDisplayRegistration, registration: Registration) =>
      auditService.audit(EtmpDisplayRegistrationAuditModel.build(EtmpRegistrationAuditType.DisplayRegistration, etmpRegistration, registration))

    if (appConfig.registrationCacheEnabled) {
      cachedRegistrationRepository.get(request.userId).flatMap {
        case Some(cachedRegistration) =>
          Future.successful(cachedRegistration.registration)
        case _ =>
          getRegistration(vrn, auditBlock).map { maybeRegistration =>
            cachedRegistrationRepository.set(request.userId, maybeRegistration)
            maybeRegistration
          }
      }
    } else {
      getRegistration(vrn, auditBlock)
    }

  }

  def getWithoutAudit(vrn: Vrn)(implicit headerCarrier: HeaderCarrier): Future[Option[Registration]] = {
    val emptyAuditBlock = (_: EtmpDisplayRegistration, _: Registration) => ()

    getRegistration(vrn, emptyAuditBlock)
  }

  private def getRegistration(vrn: Vrn,
                              auditBlock: (EtmpDisplayRegistration, Registration) => Unit)(implicit hc: HeaderCarrier): Future[Option[Registration]] = {
    registrationConnector.get(vrn).flatMap {
      case Right(etmpRegistration) =>
        getVatInfoConnector.getVatCustomerDetails(vrn).flatMap {
          case Right(vatDetails) =>

            val registration = Registration.fromEtmpRegistration(
              vrn,
              vatDetails,
              etmpRegistration.tradingNames,
              etmpRegistration.schemeDetails,
              etmpRegistration.bankDetails,
              etmpRegistration.adminUse
            )
            getTransferringMsidEffectiveFromDate(registration).map { transferringMsidEffectiveFromDate =>
              val registrationWithExcludedTraderAndPartialReturnPeriod = registration.copy(transferringMsidEffectiveFromDate = transferringMsidEffectiveFromDate)
              auditBlock(etmpRegistration, registrationWithExcludedTraderAndPartialReturnPeriod)
              Some(registrationWithExcludedTraderAndPartialReturnPeriod)
            }
          case Left(error) =>
            logger.info(s"There was an error getting customer VAT information from DES: ${error.body}")
            Future.failed(new Exception(s"There was an error getting customer VAT information from DES: ${error.body}"))
        }
      case Left(EisDisplayRegistrationError(eisDisplayErrorResponse)) if eisDisplayErrorResponse.errorDetail.errorCode == EisDisplayErrorResponse.displayErrorCodeNoRegistration =>
        logger.info(s"There was no Registration from ETMP found")
        Future.successful(None)
      case Left(error) =>
        logger.error(s"There was an error getting Registration from ETMP: ${error.body}")
        throw EtmpException(s"There was an error getting Registration from ETMP: ${error.body}")
    }
  }

  private def getTransferringMsidEffectiveFromDate(registration: Registration): Future[Option[LocalDate]] = {
    val hasPreviousRegistration = registration.previousRegistrations.nonEmpty

    if (hasPreviousRegistration) {
      val filteredPreviousRegistrations = registration.previousRegistrations.collectFirst {
        case previousRegistration: PreviousRegistrationNew if
          previousRegistration.previousSchemesDetails.exists(_.previousScheme == PreviousScheme.OSSU) =>
          previousRegistration
      }
      filteredPreviousRegistrations.map { previousRegistration =>
          val previousOssRegistration = previousRegistration.previousSchemesDetails.find(_.previousScheme == PreviousScheme.OSSU).get
          coreValidationService.searchScheme(
            searchNumber = previousOssRegistration.previousSchemeNumbers.previousSchemeNumber,
            previousScheme = PreviousScheme.OSSU,
            intermediaryNumber = None,
            countryCode = previousRegistration.country.code
          ).map {
            case Some(coreRegistrationMatch) if coreRegistrationMatch.matchType == MatchType.TransferringMSID =>
              coreRegistrationMatch.exclusionEffectiveDate.map(LocalDate.parse)
            case _ =>
              None
          }
      }.getOrElse(Future.successful(None))
    } else {
      Future.successful(None)
    }
  }

  def amend(amendRegistrationRequest: AmendRegistrationRequest)(implicit hc: HeaderCarrier, request: AuthorisedMandatoryRegistrationRequest[_]): Future[AmendResult] = {

    val auditBlock = (etmpRegistrationRequest: EtmpAmendRegistrationRequest, amendRegistrationResponse: AmendRegistrationResponse) =>
      auditService.audit(EtmpAmendRegistrationAuditModel.build(EtmpRegistrationAuditType.AmendRegistration, etmpRegistrationRequest, None, Some(amendRegistrationResponse), None, SubmissionResult.Success))

    val errorAuditBlock = (etmpRegistrationRequest: EtmpAmendRegistrationRequest) =>
      auditService.audit(EtmpAmendRegistrationAuditModel.build(EtmpRegistrationAuditType.AmendRegistration, etmpRegistrationRequest, None, None, None, SubmissionResult.Failure))

    val amendmentResult = amendRegistration(
      request.registration,
      amendRegistrationRequest,
      auditBlock,
      errorAuditBlock
    )

    if (appConfig.registrationCacheEnabled) {
      cachedRegistrationRepository.clear(request.userId)
    }
    amendmentResult
  }

  private def amendRegistration(registration: Registration,
                                amendRegistrationRequest: AmendRegistrationRequest,
                                auditBlock: (EtmpAmendRegistrationRequest, AmendRegistrationResponse) => Unit,
                                failureAuditBlock: EtmpAmendRegistrationRequest => Unit): Future[AmendResult] = {
    val etmpRegistrationRequest = EtmpAmendRegistrationRequest.fromRegistrationRequest(registration, amendRegistrationRequest, EtmpMessageType.OSSSubscriptionAmend)
    registrationConnector.amendRegistration(etmpRegistrationRequest).flatMap {
      case Right(amendRegistrationResponse) =>
        auditBlock(etmpRegistrationRequest, amendRegistrationResponse)

        logger.info(s"Successfully sent amend registration to ETMP at ${amendRegistrationResponse.processingDateTime} for vrn ${amendRegistrationResponse.vrn}")
        Future.successful(AmendSucceeded)
      case Left(e) =>
        logger.error(s"An error occurred while amending registration ${e.getClass} ${e.body}")
        failureAuditBlock(etmpRegistrationRequest)
        throw EtmpException(s"There was an error amending Registration from ETMP: ${e.getClass} ${e.body}")
    }
  }
}
