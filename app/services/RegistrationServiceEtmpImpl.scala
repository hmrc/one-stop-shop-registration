/*
 * Copyright 2023 HM Revenue & Customs
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
import models.repository.InsertResult.{AlreadyExists, InsertSucceeded}
import models.enrolments.EtmpEnrolmentErrorResponse
import models.etmp.{AmendRegistrationResponse, DisplayRegistration, EtmpMessageType, EtmpRegistrationRequest, EtmpRegistrationStatus}
import models.requests.RegistrationRequest
import models._
import models.audit.{EtmpDisplayRegistrationAuditModel, EtmpRegistrationAuditModel, EtmpRegistrationAuditType, SubmissionResult}
import models.core.EisDisplayErrorResponse
import models.repository.{AmendResult, InsertResult}
import models.repository.AmendResult.AmendSucceeded
import play.api.http.Status.NO_CONTENT
import repositories.{RegistrationRepository, RegistrationStatusRepository}
import services.exclusions.ExclusionService
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationServiceEtmpImpl @Inject()(
                                             registrationConnector: RegistrationConnector,
                                             enrolmentsConnector: EnrolmentsConnector,
                                             getVatInfoConnector: GetVatInfoConnector,
                                             registrationRepository: RegistrationRepository,
                                             registrationStatusRepository: RegistrationStatusRepository,
                                             retryService: RetryService,
                                             appConfig: AppConfig,
                                             exclusionService: ExclusionService,
                                             auditService: AuditService,
                                             clock: Clock
                                           )(implicit ec: ExecutionContext) extends RegistrationService {

  def createRegistration(registrationRequest: RegistrationRequest)
                        (implicit hc: HeaderCarrier, request: AuthorisedMandatoryVrnRequest[_]): Future[InsertResult] = {
    val etmpRegistrationRequest = EtmpRegistrationRequest.fromRegistrationRequest(registrationRequest, EtmpMessageType.OSSSubscriptionCreate)
    registrationConnector.create(etmpRegistrationRequest).flatMap {
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
  }

  def get(vrn: Vrn)(implicit headerCarrier: HeaderCarrier, request: AuthorisedMandatoryVrnRequest[_]): Future[Option[Registration]] = {
    val auditBlock = (etmpRegistration: DisplayRegistration, registration: Registration) =>
      auditService.audit(EtmpDisplayRegistrationAuditModel.build(EtmpRegistrationAuditType.DisplayRegistration, etmpRegistration, registration))

    getRegistration(vrn, auditBlock)
  }

  def getWithoutAudit(vrn: Vrn)(implicit headerCarrier: HeaderCarrier): Future[Option[Registration]] = {
    val emptyAuditBlock = (_: DisplayRegistration, _: Registration) => ()

    getRegistration(vrn, emptyAuditBlock)
  }

  private def getRegistration(vrn: Vrn,
                              auditBlock: (DisplayRegistration, Registration) => Unit)(implicit hc: HeaderCarrier): Future[Option[Registration]] = {
    registrationConnector.get(vrn).flatMap {
      case Right(etmpRegistration) =>
        getVatInfoConnector.getVatCustomerDetails(vrn).flatMap {
          case Right(vatDetails) =>

            val registration = Registration.fromEtmpRegistration(
              vrn,
              vatDetails,
              etmpRegistration.tradingNames,
              etmpRegistration.schemeDetails,
              etmpRegistration.bankDetails
            )

            if (appConfig.exclusionsEnabled) {
              exclusionService.findExcludedTrader(registration.vrn).map { maybeExcludedTrader =>
                val registrationWithExcludedTrader = registration.copy(excludedTrader = maybeExcludedTrader)
                auditBlock(etmpRegistration, registrationWithExcludedTrader)
                Some(registrationWithExcludedTrader)
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
  }

  def amend(registrationRequest: RegistrationRequest)(implicit hc: HeaderCarrier, request: AuthorisedMandatoryVrnRequest[_]): Future[AmendResult] = {

    val auditBlock = (etmpRegistrationRequest: EtmpRegistrationRequest, amendRegistrationResponse: AmendRegistrationResponse) =>
      auditService.audit(EtmpRegistrationAuditModel.build(EtmpRegistrationAuditType.AmendRegistration, etmpRegistrationRequest, None, Some(amendRegistrationResponse), None, SubmissionResult.Success))

    val errorAuditBlock = (etmpRegistrationRequest: EtmpRegistrationRequest) =>
      auditService.audit(EtmpRegistrationAuditModel.build(EtmpRegistrationAuditType.AmendRegistration, etmpRegistrationRequest, None, None, None, SubmissionResult.Failure))

    amendRegistration(
      registrationRequest,
      auditBlock,
      errorAuditBlock
    )
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
