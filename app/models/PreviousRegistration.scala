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

package models

import crypto.EncryptedValue
import play.api.libs.json.{Json, OFormat}

case class PreviousRegistration(country: Country, previousSchemesDetails: Seq[PreviousSchemeDetails])

object PreviousRegistration {

  implicit val format: OFormat[PreviousRegistration] = Json.format[PreviousRegistration]
}

case class EncryptedPreviousRegistration(country: EncryptedCountry, previousSchemeDetails: Seq[EncryptedPreviousSchemeDetails])

object EncryptedPreviousRegistration {

  implicit val format: OFormat[EncryptedPreviousRegistration] = Json.format[EncryptedPreviousRegistration]
}

case class PreviousSchemeDetails(previousScheme: PreviousScheme, previousSchemeNumbers: PreviousSchemeNumbers)

object PreviousSchemeDetails {

  implicit val format: OFormat[PreviousSchemeDetails] = Json.format[PreviousSchemeDetails]
}


case class EncryptedPreviousSchemeDetails(previousScheme: EncryptedValue, previousSchemeNumbers: EncryptedPreviousSchemeNumbers)

object EncryptedPreviousSchemeDetails {

  implicit val format: OFormat[EncryptedPreviousSchemeDetails] = Json.format[EncryptedPreviousSchemeDetails]
}

case class PreviousSchemeNumbers(
                                  previousSchemeNumber: String,
                                  previousIntermediaryNumber: Option[String]
                                )

object PreviousSchemeNumbers {

  implicit val format: OFormat[PreviousSchemeNumbers] = Json.format[PreviousSchemeNumbers]
}

case class EncryptedPreviousSchemeNumbers(
                                  previousSchemeNumber: EncryptedValue,
                                  previousIntermediaryNumber: Option[EncryptedValue]
                                )

object EncryptedPreviousSchemeNumbers {

  implicit val format: OFormat[EncryptedPreviousSchemeNumbers] = Json.format[EncryptedPreviousSchemeNumbers]
}
