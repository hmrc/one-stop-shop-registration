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
import play.api.libs.json._

case class FixedEstablishmentAddress (
                                       line1: String,
                                       line2: Option[String],
                                       townOrCity: String,
                                       county: Option[String],
                                       postCode: Option[String]
                                     )

object FixedEstablishmentAddress {
  implicit val format: OFormat[FixedEstablishmentAddress] = Json.format[FixedEstablishmentAddress]
}

case class EncryptedFixedEstablishmentAddress (
                                                line1: EncryptedValue,
                                                line2: Option[EncryptedValue],
                                                townOrCity: EncryptedValue,
                                                county: Option[EncryptedValue],
                                                postCode: Option[EncryptedValue]
                                              )

object EncryptedFixedEstablishmentAddress {

  implicit val format: OFormat[EncryptedFixedEstablishmentAddress] =
    Json.format[EncryptedFixedEstablishmentAddress]
}
