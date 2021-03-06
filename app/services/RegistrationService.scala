/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.RegistrationHttpParser.ValidateRegistrationResponse
import logging.Logging
import models.requests.RegistrationRequest
import models.{InsertResult, Registration}
import uk.gov.hmrc.domain.Vrn

import java.time.{Clock, Instant}
import scala.concurrent.Future

trait RegistrationService extends Logging {

  def buildRegistration(request: RegistrationRequest, clock: Clock): Registration =
    Registration(
      vrn = request.vrn,
      registeredCompanyName = request.registeredCompanyName,
      tradingNames = request.tradingNames,
      vatDetails = request.vatDetails,
      euRegistrations = request.euRegistrations,
      contactDetails = request.contactDetails,
      websites = request.websites,
      commencementDate = request.commencementDate,
      previousRegistrations = request.previousRegistrations,
      bankDetails = request.bankDetails,
      isOnlineMarketplace = request.isOnlineMarketplace,
      niPresence = request.niPresence,
      dateOfFirstSale = request.dateOfFirstSale,
      submissionReceived = Instant.now(clock),
      lastUpdated = Instant.now(clock)
    )

  def createRegistration(request: RegistrationRequest): Future[InsertResult]

  def get(vrn: Vrn): Future[Option[Registration]]

  def validate(vrn: Vrn): Future[ValidateRegistrationResponse]
}


