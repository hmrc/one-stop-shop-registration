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

package uk.gov.hmrc.onestopshopregistration.models.des

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, OWrites, Reads, __}

import java.time.LocalDate

case class VatCustomerInfo(
                            registrationDate: LocalDate,
                            postCode: String
                          )

object VatCustomerInfo {

  val desReads: Reads[VatCustomerInfo] =
    (
      (__ \ "approvedInformation" \ "customerDetails" \ "effectiveRegistrationDate").read[LocalDate] and
      (__ \ "approvedInformation" \ "PPOB" \ "address" \ "postCode").read[String]
    )(VatCustomerInfo.apply _)

  implicit val writes: OWrites[VatCustomerInfo] =
    Json.writes[VatCustomerInfo]
}
