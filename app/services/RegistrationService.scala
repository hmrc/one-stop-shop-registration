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

import controllers.actions.{AuthorisedMandatoryRegistrationRequest, AuthorisedMandatoryVrnRequest}
import logging.Logging
import models.Registration
import models.repository.{AmendResult, InsertResult}
import models.requests.{AmendRegistrationRequest, RegistrationRequest}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait RegistrationService extends Logging {

  def createRegistration(registrationRequest: RegistrationRequest)
                        (implicit hc: HeaderCarrier, request: AuthorisedMandatoryVrnRequest[_]): Future[InsertResult]

  def get(vrn: Vrn)(implicit hc: HeaderCarrier, request: AuthorisedMandatoryVrnRequest[_]): Future[Option[Registration]]

  def getWithoutAudit(vrn: Vrn)(implicit hc: HeaderCarrier): Future[Option[Registration]]

  def amend(amendRegistrationRequest: AmendRegistrationRequest)(implicit hc: HeaderCarrier, request: AuthorisedMandatoryRegistrationRequest[_]): Future[AmendResult]
}


