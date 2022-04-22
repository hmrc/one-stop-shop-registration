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

package models.des

import models.DesAddress
import models.des.PartyType.{OtherPartyType, VatGroup}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, OWrites, Reads, __}

import java.time.LocalDate

case class VatCustomerInfo(
                            address: DesAddress,
                            registrationDate: Option[LocalDate],
                            partOfVatGroup: Option[Boolean],
                            organisationName: Option[String]
                          )

object VatCustomerInfo {

  private def fromDesPayload(
                              address: DesAddress,
                              registrationDate: Option[LocalDate],
                              partyType: Option[PartyType],
                              organisationName: Option[String]
                            ): VatCustomerInfo =
    VatCustomerInfo(
      address          = address,
      registrationDate = registrationDate,
      partOfVatGroup   = partyType map {
        case VatGroup       => true
        case OtherPartyType => false
      },
      organisationName = organisationName
    )

  implicit val desReads: Reads[VatCustomerInfo] =
    (
      (__ \ "approvedInformation" \ "PPOB" \ "address").read[DesAddress] and
      (__ \ "approvedInformation" \ "customerDetails" \ "effectiveRegistrationDate").readNullable[LocalDate] and
      (__ \ "approvedInformation" \ "customerDetails" \ "partyType").readNullable[PartyType] and
      (__ \ "approvedInformation" \ "customerDetails" \ "organisationName").readNullable[String]
    )(VatCustomerInfo.fromDesPayload _)

  implicit val writes: OWrites[VatCustomerInfo] =
    Json.writes[VatCustomerInfo]
}
