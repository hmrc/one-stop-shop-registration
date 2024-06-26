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
import models.requests.{AmendRegistrationRequest, RegistrationRequest}
import models.Registration
import models.etmp.AdminUse
import models.repository.{AmendResult, InsertResult}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant, LocalDateTime}
import scala.concurrent.Future

trait RegistrationService extends Logging {

  def buildRegistration(request: RegistrationRequest, clock: Clock): Registration = {
    val now = Instant.now(clock)

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
      submissionReceived = Some(request.submissionReceived.getOrElse(now)),
      lastUpdated = Some(now),
      nonCompliantReturns = request.nonCompliantReturns,
      nonCompliantPayments = request.nonCompliantPayments,
      adminUse = AdminUse(Some(LocalDateTime.ofInstant(now, clock.getZone)))
    )
  }

  def createRegistration(registrationRequest: RegistrationRequest)
                        (implicit hc: HeaderCarrier, request: AuthorisedMandatoryVrnRequest[_]): Future[InsertResult]

  def get(vrn: Vrn)(implicit hc: HeaderCarrier, request: AuthorisedMandatoryVrnRequest[_]): Future[Option[Registration]]

  def getWithoutAudit(vrn: Vrn)(implicit hc: HeaderCarrier): Future[Option[Registration]]

  def amend(amendRegistrationRequest: AmendRegistrationRequest)(implicit hc: HeaderCarrier, request: AuthorisedMandatoryRegistrationRequest[_]): Future[AmendResult]
}


