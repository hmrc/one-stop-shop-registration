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
import controllers.actions.AuthorisedMandatoryVrnRequest
import models._
import models.audit.{EtmpDisplayRegistrationAuditModel, EtmpRegistrationAuditModel, EtmpRegistrationAuditType, SubmissionResult}
import models.core.{EisDisplayErrorResponse, MatchType}
import models.enrolments.EtmpEnrolmentErrorResponse
import models.etmp._
import models.repository.{AmendResult, InsertResult}
import models.repository.AmendResult.AmendSucceeded
import models.repository.InsertResult.{AlreadyExists, InsertSucceeded}
import models.requests.RegistrationRequest
import play.api.http.Status.NO_CONTENT
import repositories.{CachedRegistrationRepository, RegistrationRepository, RegistrationStatusRepository}
import services.exclusions.ExclusionService
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, LocalDate}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationServiceEtmpImpl @Inject()(
                                             registrationConnector: RegistrationConnector,
                                             enrolmentsConnector: EnrolmentsConnector,
                                             getVatInfoConnector: GetVatInfoConnector,
                                             registrationRepository: RegistrationRepository,
                                             registrationStatusRepository: RegistrationStatusRepository,
                                             cachedRegistrationRepository: CachedRegistrationRepository,
                                             retryService: RetryService,
                                             appConfig: AppConfig,
                                             exclusionService: ExclusionService,
                                             auditService: AuditService,
                                             coreValidationService: CoreValidationService,
                                             clock: Clock
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
              if (appConfig.duplicateRegistrationIntoRepository) {
                retryService.getEtmpRegistrationStatus(appConfig.maxRetryCount, appConfig.delay, response.formBundleNumber).flatMap {
                  case EtmpRegistrationStatus.Success =>
                    logger.info("Insert succeeded")
                    registrationRepository.insert(buildRegistration(registrationRequest, clock))
                  case _ =>
                    logger.error(s"Failed to add enrolment")
                    registrationStatusRepository.set(RegistrationStatus(subscriptionId = response.formBundleNumber, status = EtmpRegistrationStatus.Error))
                    throw EtmpException("Failed to add enrolment")
                }
              } else {
                Future.successful(InsertSucceeded)
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
    val auditBlock = (etmpRegistration: DisplayRegistration, registration: Registration) =>
      auditService.audit(EtmpDisplayRegistrationAuditModel.build(EtmpRegistrationAuditType.DisplayRegistration, etmpRegistration, registration))

    if (appConfig.registrationCacheEnabled) {
      cachedRegistrationRepository.get(request.userId).flatMap {
        case Some(cachedRegistration) => Future.successful(cachedRegistration.registration)
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
    val emptyAuditBlock = (_: DisplayRegistration, _: Registration) => ()

    getRegistration(vrn, emptyAuditBlock)
  }

  private def getRegistration(vrn: Vrn,
                              auditBlock: (DisplayRegistration, Registration) => Unit)(implicit hc: HeaderCarrier): Future[Option[Registration]] = {
    if (appConfig.displayRegistrationEndpointEnabled) {
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

              if (appConfig.exclusionsEnabled) {
                exclusionService.findExcludedTrader(registration.vrn).flatMap { maybeExcludedTrader =>
                  getTransferringMsidEffectiveFromDate(registration).map { transferringMsidEffectiveFromDate =>
                    val registrationWithExcludedTraderAndPartialReturnPeriod = registration.copy(excludedTrader = maybeExcludedTrader, transferringMsidEffectiveFromDate = transferringMsidEffectiveFromDate)
                    auditBlock(etmpRegistration, registrationWithExcludedTraderAndPartialReturnPeriod)
                    Some(registrationWithExcludedTraderAndPartialReturnPeriod)
                  }
                }
              } else {
                auditBlock(etmpRegistration, registration)
                Future.successful(Some(registration))
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
    } else {
      (for {
        maybeRegistration <- registrationRepository.get(vrn)
        maybeExcludedTrader <- exclusionService.findExcludedTrader(vrn)
      } yield {
        maybeRegistration match {
          case Some(registration) =>
            getTransferringMsidEffectiveFromDate(registration).map { transferringMsidEffectiveFromDate =>
              Some(registration.copy(excludedTrader = maybeExcludedTrader, transferringMsidEffectiveFromDate = transferringMsidEffectiveFromDate))
            }
          case _ => Future.successful(None)
        }
      }).flatten
    }
  }

  private def getTransferringMsidEffectiveFromDate(registration: Registration)
                                                  (implicit hc: HeaderCarrier): Future[Option[LocalDate]] = {
    val hasPreviousRegistration = registration.previousRegistrations.nonEmpty

    if (hasPreviousRegistration) {
      val filteredPreviousRegistrations = registration.previousRegistrations.find {
        case previousRegistration: PreviousRegistrationNew =>
          previousRegistration.previousSchemesDetails.exists(_.previousScheme == PreviousScheme.OSSU)
      }
      filteredPreviousRegistrations.map {
        case previousRegistration: PreviousRegistrationNew =>
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

  def amend(registrationRequest: RegistrationRequest)(implicit hc: HeaderCarrier, request: AuthorisedMandatoryVrnRequest[_]): Future[AmendResult] = {

    val auditBlock = (etmpRegistrationRequest: EtmpRegistrationRequest, amendRegistrationResponse: AmendRegistrationResponse) =>
      auditService.audit(EtmpRegistrationAuditModel.build(EtmpRegistrationAuditType.AmendRegistration, etmpRegistrationRequest, None, Some(amendRegistrationResponse), None, SubmissionResult.Success))

    val errorAuditBlock = (etmpRegistrationRequest: EtmpRegistrationRequest) =>
      auditService.audit(EtmpRegistrationAuditModel.build(EtmpRegistrationAuditType.AmendRegistration, etmpRegistrationRequest, None, None, None, SubmissionResult.Failure))

    val amendmentResult = amendRegistration(
      registrationRequest,
      auditBlock,
      errorAuditBlock
    )

    if (appConfig.registrationCacheEnabled) {
      cachedRegistrationRepository.clear(request.userId)
    }
    amendmentResult
  }

  def amendWithoutAudit(registrationRequest: RegistrationRequest)(implicit hc: HeaderCarrier): Future[AmendResult] = {

    val emptyAuditBlock = (_: EtmpRegistrationRequest, _: AmendRegistrationResponse) => ()
    val emptyFailureAuditBlock = (_: EtmpRegistrationRequest) => ()

    amendRegistration(
      registrationRequest,
      emptyAuditBlock,
      emptyFailureAuditBlock
    )
  }

  private def amendRegistration(registrationRequest: RegistrationRequest,
                                auditBlock: (EtmpRegistrationRequest, AmendRegistrationResponse) => Unit,
                                failureAuditBlock: EtmpRegistrationRequest => Unit): Future[AmendResult] = {
    val etmpRegistrationRequest = EtmpRegistrationRequest.fromRegistrationRequest(registrationRequest, EtmpMessageType.OSSSubscriptionAmend)
    registrationConnector.amendRegistration(etmpRegistrationRequest).flatMap {
      case Right(amendRegistrationResponse) =>
        auditBlock(etmpRegistrationRequest, amendRegistrationResponse)

        logger.info(s"Successfully sent amend registration to ETMP at ${amendRegistrationResponse.processingDateTime} for vrn ${amendRegistrationResponse.vrn}")
        if (appConfig.duplicateRegistrationIntoRepository) {
          registrationRepository.set(buildRegistration(registrationRequest, clock))
        } else {
          Future.successful(AmendSucceeded)
        }
      case Left(e) =>
        logger.error(s"An error occurred while amending registration ${e.getClass} ${e.body}")
        failureAuditBlock(etmpRegistrationRequest)
        throw EtmpException(s"There was an error amending Registration from ETMP: ${e.getClass} ${e.body}")
    }
  }
}
