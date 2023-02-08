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
import models.etmp.EtmpRegistrationRequest
import models.requests.RegistrationRequest
import models.{Conflict, EtmpEnrolmentError, EtmpException, InsertResult, Registration}
import play.api.http.Status.NO_CONTENT
import services.exclusions.ExclusionService
import uk.gov.hmrc.domain.Vrn

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationServiceEtmpImpl @Inject()(
                                             registrationConnector: RegistrationConnector,
                                             enrolmentsConnector: EnrolmentsConnector,
                                             appConfig: AppConfig,
                                             exclusionService: ExclusionService
                                           )(implicit ec: ExecutionContext) extends RegistrationService {

  def createRegistration(request: RegistrationRequest): Future[InsertResult] = {
    if (appConfig.addEnrolment) {
      val registrationRequest = EtmpRegistrationRequest.fromRegistrationRequest(request)
      registrationConnector.createWithEnrolment(registrationRequest).flatMap {
        case Right(response) =>
          enrolmentsConnector.confirmEnrolment(response.formBundleNumber).map { enrolmentResponse =>
            enrolmentResponse.status match {
              case NO_CONTENT =>
                logger.info("Insert succeeded")
                InsertSucceeded
              case status =>
                logger.error(s"Failed to add enrolment - $status with body ${enrolmentResponse.body}")
                throw EtmpException(s"Failed to add enrolment - ${enrolmentResponse.body}")
            }
          }
        case Left(EtmpEnrolmentError(EtmpEnrolmentErrorResponse.alreadyActiveSubscriptionErrorCode, _)) =>
          logger.warn("Enrolment already existed")
          Future.successful(AlreadyExists)
        case Left(error) =>
          logger.error(s"There was an error creating Registration enrolment from ETMP: $error")
          throw EtmpException(s"There was an error creating Registration enrolment from ETMP: ${error.body}")
      }
    } else {
      registrationConnector.create(request).map {
        case Right(_) => InsertSucceeded
        case Left(Conflict) => AlreadyExists
        case Left(error) => throw EtmpException(s"There was an error getting Registration from ETMP: ${error.body}")
      }
    }
  }

  def get(vrn: Vrn): Future[Option[Registration]] = {
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
