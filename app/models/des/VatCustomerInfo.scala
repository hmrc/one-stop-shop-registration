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

package models.des

import models.des.PartyType.{OtherPartyType, VatGroup}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, OFormat, OWrites, Reads, __}

import java.time.LocalDate

case class VatCustomerInfo(
                            address: DesAddress,
                            registrationDate: Option[LocalDate],
                            partOfVatGroup: Option[Boolean]
                          )

object VatCustomerInfo {

  private def fromDesPayload(address: DesAddress, registrationDate: Option[LocalDate], partyType: Option[PartyType]): VatCustomerInfo =
    VatCustomerInfo(
      address          = address,
      registrationDate = registrationDate,
      partOfVatGroup   = partyType map {
        case VatGroup       => true
        case OtherPartyType => false
      }
    )

  val desReads: Reads[VatCustomerInfo] =
    (
      (__ \ "approvedInformation" \ "PPOB" \ "address").read[DesAddress] and
      (__ \ "approvedInformation" \ "customerDetails" \ "effectiveRegistrationDate").readNullable[LocalDate] and
      (__ \ "approvedInformation" \ "customerDetails" \ "partyType").readNullable[PartyType]
    )(VatCustomerInfo.fromDesPayload _)

  implicit val writes: OWrites[VatCustomerInfo] =
    Json.writes[VatCustomerInfo]
}

case class DesAddress(
                       line1: String,
                       line2: Option[String],
                       line3: Option[String],
                       line4: Option[String],
                       line5: Option[String],
                       postCode: Option[String],
                       countryCode: String
                     )

object DesAddress {

  implicit val format: OFormat[DesAddress] =
    Json.format[DesAddress]
}
