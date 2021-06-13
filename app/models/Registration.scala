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

package models

import crypto.EncryptedValue
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate

case class Registration(
                         vrn: Vrn,
                         registeredCompanyName: String,
                         tradingNames: Seq[String],
                         vatDetails: VatDetails,
                         euRegistrations: Seq[EuTaxRegistration],
                         contactDetails: ContactDetails,
                         websites: Seq[String],
                         startDate: LocalDate,
                         currentCountryOfRegistration: Option[Country],
                         previousRegistrations: Seq[PreviousRegistration],
                         bankDetails: BankDetails
                       )

object Registration {
  implicit val format: OFormat[Registration] = Json.format[Registration]
}

case class EncryptedRegistration(
                                  vrn: Vrn,
                                  registeredCompanyName: EncryptedValue,
                                  tradingNames: Seq[EncryptedValue],
                                  vatDetails: EncryptedVatDetails,
                                  euRegistrations: Seq[EncryptedEuTaxRegistration],
                                  contactDetails: EncryptedContactDetails,
                                  websites: Seq[EncryptedValue],
                                  startDate: LocalDate,
                                  currentCountryOfRegistration: Option[EncryptedCountry],
                                  previousRegistrations: Seq[EncryptedPreviousRegistration],
                                  bankDetails: EncryptedBankDetails
                                )

object EncryptedRegistration {

  implicit val format: OFormat[EncryptedRegistration] = Json.format[EncryptedRegistration]
}