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

package controllers.external

import controllers.actions.AuthAction
import logging.Logging
import models.external._
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.external.ExternalEntryService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ExternalEntryController @Inject()(
                                        cc: ControllerComponents,
                                        externalEntryService: ExternalEntryService,
                                        auth: AuthAction
                                      )(implicit ec: ExecutionContext)
  extends BackendController(cc) with Logging {


  def onExternal(lang: Option[String] = None): Action[ExternalRequest] = auth(parse.json[ExternalRequest]).async {
    implicit request =>
      externalEntryService.getExternalResponse(request.body, request.userId, lang) map {
        response =>
          Ok(Json.toJson(response))
      }
  }

  def getExternalEntry(): Action[AnyContent] = auth.async {
    implicit request =>
      externalEntryService.getSavedResponseUrl(request.userId).map {
        response =>
          Ok(Json.toJson(ExternalEntryUrlResponse(response)))
      }
  }
}

