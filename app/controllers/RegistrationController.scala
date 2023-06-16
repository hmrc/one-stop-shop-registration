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

import controllers.actions.AuthenticatedControllerComponents
import models.repository.AmendResult.AmendSucceeded
import models.repository.InsertResult.{AlreadyExists, InsertSucceeded}
import models.requests.RegistrationRequest
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.RegistrationService
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RegistrationController @Inject()(
                                        cc: AuthenticatedControllerComponents,
                                        registrationService: RegistrationService
                                      )(implicit ec: ExecutionContext) extends BackendController(cc) {

  def create(): Action[RegistrationRequest] = cc.authAndRequireVat()(parse.json[RegistrationRequest]).async {
    implicit request =>
      registrationService
        .createRegistration(request.body)
        .map{
          case InsertSucceeded => Created
          case AlreadyExists => Conflict
        }
  }

  def get: Action[AnyContent] = cc.authAndRequireVat().async {
    implicit request =>
      registrationService.get(request.vrn) map {
        case Some(registration) => Ok(Json.toJson(registration))
        case None => NotFound
      }
  }

  def getByVrn(vrn: String): Action[AnyContent] = cc.authAndRequireVat().async {
    implicit request =>
      registrationService.get(Vrn(vrn)) map {
        case Some(registration) => Ok(Json.toJson(registration))
        case None               => NotFound
      }
  }

  def amend(): Action[RegistrationRequest] = cc.authAndRequireVat()(parse.json[RegistrationRequest]).async {
    implicit request =>
      registrationService
        .amend(request.body)
        .map {
          case AmendSucceeded => Ok
        }
  }
}
