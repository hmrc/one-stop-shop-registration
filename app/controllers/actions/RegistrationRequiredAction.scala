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

package controllers.actions

import logging.Logging
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{ActionRefiner, Result}
import services.RegistrationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationRequiredAction @Inject()(
                                            registrationService: RegistrationService
                                          )(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[AuthorisedMandatoryVrnRequest, AuthorisedMandatoryRegistrationRequest] with Logging {

  override protected def refine[A](request: AuthorisedMandatoryVrnRequest[A]): Future[Either[Result, AuthorisedMandatoryRegistrationRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    implicit val r: AuthorisedMandatoryVrnRequest[A] = request

    registrationService.get(request.vrn).map {
      case Some(registration) =>
        Right(AuthorisedMandatoryRegistrationRequest(request.request, request.userId, request.vrn, registration))
      case _ =>
        Left(Unauthorized)
    }
  }
}
