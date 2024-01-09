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

package models.etmp

import models.BankDetails
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Json, OWrites, Reads}


case class DisplayRegistration(
                              tradingNames: Seq[EtmpTradingNames],
                              schemeDetails: EtmpSchemeDetails,
                              bankDetails: BankDetails,
                              adminUse: AdminUse
                              )


object DisplayRegistration {

  private def fromDisplayRegistrationPayload(
                                              tradingNames: Option[Seq[EtmpTradingNames]],
                                              schemeDetails: EtmpSchemeDetails,
                                              bankDetails: BankDetails,
                                              adminUse: AdminUse
                                            ): DisplayRegistration =
    DisplayRegistration(
      tradingNames = tradingNames.fold(Seq.empty[EtmpTradingNames])(a => a),
      schemeDetails = schemeDetails,
      bankDetails = bankDetails,
      adminUse = adminUse
    )

  implicit val reads: Reads[DisplayRegistration] =
    (
      (__ \ "tradingNames").readNullable[Seq[EtmpTradingNames]] and
        (__ \ "schemeDetails").read[EtmpSchemeDetails] and
        (__ \ "bankDetails").read[BankDetails] and
        (__ \ "adminUse").read[AdminUse]
      )(DisplayRegistration.fromDisplayRegistrationPayload _)

  implicit val writes: OWrites[DisplayRegistration] =
    Json.writes[DisplayRegistration]

}