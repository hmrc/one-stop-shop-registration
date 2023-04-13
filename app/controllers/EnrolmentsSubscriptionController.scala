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

package controllers

import config.AppConfig
import connectors.EnrolmentsConnector
import controllers.actions.AuthAction
import logging.Logging
import models.enrolments.EnrolmentStatus
import models.{EtmpException, RegistrationStatus}
import models.etmp.EtmpRegistrationStatus
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.RegistrationStatusRepository
import services.RetryService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnrolmentsSubscriptionController @Inject()(
                                                  cc: ControllerComponents,
                                                  enrolmentsConnector: EnrolmentsConnector,
                                                  registrationStatusRepository: RegistrationStatusRepository,
                                                  retryService: RetryService,
                                                  appConfig: AppConfig,
                                                  auth: AuthAction
                                                )(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def authoriseEnrolment(subscriptionId: String): Action[JsValue] =
    Action.async(parse.json) {
      implicit request =>
        val enrolmentStatus = (request.body \ "state").as[EnrolmentStatus]
        if (enrolmentStatus == EnrolmentStatus.Success) {
          logger.info(s"Enrolment complete for $subscriptionId, enrolment state = $enrolmentStatus")
          registrationStatusRepository.set(RegistrationStatus(subscriptionId,
            status = EtmpRegistrationStatus.Success))
        } else {
          logger.error(s"Enrolment failure for $subscriptionId, enrolment state = $enrolmentStatus ${request.body}")
          registrationStatusRepository.set(RegistrationStatus(subscriptionId,
            status = EtmpRegistrationStatus.Error))
        }
        Future.successful(NoContent)
    }

  def confirmEnrolment(): Action[AnyContent] = auth.async {
    implicit request =>
      appConfig.subscriptionIds.find(_.vrn == request.vrn.vrn).map { traderSubscriptionId =>
        val subscriptionId = traderSubscriptionId.subscriptionId
        enrolmentsConnector.confirmEnrolment(subscriptionId).flatMap { enrolmentResponse =>
          enrolmentResponse.status match {
            case NO_CONTENT =>
              logger.info("Sent enrolment issuer call to tax-enrolments")
              Future.successful(NoContent)
              retryService.getEtmpRegistrationStatus(appConfig.maxRetryCount, appConfig.delay, subscriptionId).map {
                case EtmpRegistrationStatus.Success =>
                  logger.info("Successfully enrolled")
                  NoContent
                case _ =>
                  logger.error(s"Failed to add enrolment")
                  registrationStatusRepository.set(RegistrationStatus(subscriptionId = subscriptionId, status = EtmpRegistrationStatus.Error))
                  throw EtmpException("Failed to add enrolment")
              }
            case status =>
              logger.error(s"Failed to add enrolment - $status with body ${enrolmentResponse.body}")
              throw EtmpException(s"Failed to add enrolment - ${enrolmentResponse.body}")
          }
        }
      }.getOrElse{
        logger.error(s"No subscription id was found for user ${request.vrn}")
        Future.successful(NotFound("No subscription id found"))
      }
  }
}
