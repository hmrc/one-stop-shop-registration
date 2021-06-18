/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.actions.AuthAction
import models.InsertResult.{AlreadyExists, InsertSucceeded}
import models.requests.RegistrationRequest
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.RegistrationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RegistrationController @Inject() (
  cc: ControllerComponents,
  registrationService: RegistrationService,
  auth: AuthAction
)(implicit ec: ExecutionContext) extends BackendController(cc) {

  def create(): Action[RegistrationRequest] = auth(parse.json[RegistrationRequest]).async {
    implicit request =>
      registrationService
        .createRegistration(request.body)
        .map {
          case InsertSucceeded => Created
          case AlreadyExists   => Conflict
        }
  }

  def get: Action[AnyContent] = auth.async {
    implicit request =>
      registrationService.get(request.vrn) map {
        case Some(registration) => Ok(Json.toJson(registration))
        case None               => NotFound
      }
  }
}
