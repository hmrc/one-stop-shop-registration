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

import logging.Logging
import models.enrolments.EnrolmentStatus
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.Future

class EnrolmentsSubscriptionController @Inject()(
                                                  cc: ControllerComponents
                                                ) extends BackendController(cc) with Logging {

  def authoriseEnrolment(subscriptionId: String): Action[JsValue] =
    Action.async(parse.json) {
      implicit request =>
        val enrolmentStatus = (request.body \ "state").as[EnrolmentStatus]
        if(enrolmentStatus == EnrolmentStatus.Success) {
          logger.info(s"Enrolment complete for $subscriptionId, enrolment state = $enrolmentStatus")
        } else {
          logger.error(s"Enrolment failure for $subscriptionId, enrolment state = $enrolmentStatus ${request.body}")
        }
        Future.successful(NoContent)
    }

}
