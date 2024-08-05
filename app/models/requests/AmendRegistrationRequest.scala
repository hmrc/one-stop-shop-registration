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

package models.requests

import models._
import models.exclusions.ExclusionDetails
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.domain.Vrn

import java.time.{Instant, LocalDate}

case class AmendRegistrationRequest(
                                vrn: Vrn,
                                registeredCompanyName: String,
                                tradingNames: Seq[String],
                                vatDetails: VatDetails,
                                euRegistrations: Seq[EuTaxRegistration],
                                contactDetails: ContactDetails,
                                websites: Seq[String],
                                commencementDate: LocalDate,
                                previousRegistrations: Seq[PreviousRegistration],
                                bankDetails: BankDetails,
                                isOnlineMarketplace: Boolean,
                                niPresence: Option[NiPresence],
                                dateOfFirstSale: Option[LocalDate],
                                nonCompliantReturns: Option[String],
                                nonCompliantPayments: Option[String],
                                submissionReceived: Option[Instant],
                                exclusionDetails: Option[ExclusionDetails],
                                rejoin: Option[Boolean]
                              )

object AmendRegistrationRequest {

  implicit val format: OFormat[AmendRegistrationRequest] = Json.format[AmendRegistrationRequest]
}
