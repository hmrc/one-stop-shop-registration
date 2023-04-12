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
import connectors.{EnrolmentsConnector, RegistrationConnector}
import models.InsertResult.{AlreadyExists, InsertSucceeded}
import models.enrolments.EtmpEnrolmentErrorResponse
import models.etmp.{EtmpRegistrationRequest, EtmpRegistrationStatus}
import models.requests.RegistrationRequest
import models.{EtmpEnrolmentError, EtmpException, InsertResult, Registration, RegistrationStatus}
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
                                             registrationRepository: RegistrationRepository,
                                             registrationStatusRepository: RegistrationStatusRepository,
                                             retryService: RetryService,
                                             appConfig: AppConfig,
                                             exclusionService: ExclusionService,
                                             clock: Clock
                                           )(implicit ec: ExecutionContext) extends RegistrationService {

  def createRegistration(request: RegistrationRequest)(implicit hc: HeaderCarrier): Future[InsertResult] = {
    val registrationRequest = EtmpRegistrationRequest.fromRegistrationRequest(request)
    registrationConnector.create(registrationRequest).flatMap {
      case Right(response) =>
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
                    registrationRepository.insert(buildRegistration(request, clock))
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
        Future.successful(AlreadyExists)
      case Left(error) =>
        logger.error(s"There was an error creating Registration enrolment from ETMP: $error")
        throw EtmpException(s"There was an error creating Registration enrolment from ETMP: ${error.body}")
    }
  }

  def get(vrn: Vrn): Future[Option[Registration]] = {
    if(appConfig.duplicateRegistrationIntoRepository) {
      for {
        maybeRegistration <- registrationRepository.get(vrn)
        maybeExcludedTrader <- exclusionService.findExcludedTrader(vrn)
      } yield {
        maybeRegistration.map { registration =>
          if (appConfig.exclusionsEnabled) {
            registration.copy(excludedTrader = maybeExcludedTrader)
          } else {
            registration
          }
        }
      }
    } else {
      registrationConnector.get(vrn).flatMap {
        case Right(registration) =>
          if (appConfig.exclusionsEnabled) {
            exclusionService.findExcludedTrader(registration.vrn).map { maybeExcludedTrader =>
              Some(registration.copy(excludedTrader = maybeExcludedTrader))
            }
          } else {
            Future.successful(Some(registration))
          }
        case Left(error) =>
          logger.error(s"There was an error getting Registration from ETMP: ${error.body}")
          Future.successful(None)
      }
    }
  }

}
