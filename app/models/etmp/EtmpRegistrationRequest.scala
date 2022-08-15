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

package models.etmp

import models.requests.RegistrationRequest
import models.BankDetails
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.domain.Vrn

case class EtmpRegistrationRequest(
                             vrn: Vrn,
                             tradingNames: Seq[EtmpTradingNames],
                             schemeDetails: EtmpSchemeDetails,
                             bankDetails: BankDetails
                           ) {
}

object EtmpRegistrationRequest {

  def fromRegistrationRequest(registration: RegistrationRequest): EtmpRegistrationRequest = {
    EtmpRegistrationRequest(
      vrn = registration.vrn,
      tradingNames = registration.tradingNames.map(EtmpTradingNames(_)),
      schemeDetails = EtmpSchemeDetails(
        commencementDate = registration.commencementDate.format(EtmpSchemeDetails.dateFormatter),
        firstSaleDate = registration.dateOfFirstSale.map(_.format(EtmpSchemeDetails.dateFormatter)),
        euRegistrationDetails = registration.euRegistrations.map(registration => EtmpEuRegistrationDetails.create(registration)),
        previousEURegistrationDetails = registration.previousRegistrations.map(previous => EtmpPreviousEURegistrationDetails(previous.country.code, previous.vatNumber, SchemeType.OSSUnion)),
        onlineMarketPlace = registration.isOnlineMarketplace,
        websites = registration.websites.map(Website(_)),
        contactName = registration.contactDetails.fullName,
        businessTelephoneNumber = registration.contactDetails.telephoneNumber,
        businessEmailId = registration.contactDetails.emailAddress),
      bankDetails = registration.bankDetails
    )
  }

  implicit val format: OFormat[EtmpRegistrationRequest] = Json.format[EtmpRegistrationRequest]
}

