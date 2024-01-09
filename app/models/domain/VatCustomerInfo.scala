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

package models.domain

import crypto.EncryptedValue
import models.{DesAddress, EncryptedDesAddress}
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class VatCustomerInfo(
                            address: DesAddress,
                            registrationDate: Option[LocalDate],
                            partOfVatGroup: Option[Boolean],
                            organisationName: Option[String],
                            singleMarketIndicator: Option[Boolean]
                          )

object VatCustomerInfo {

  implicit val format: OFormat[VatCustomerInfo] = Json.format[VatCustomerInfo]
}


case class EncryptedVatCustomerInfo(
                                     address: EncryptedDesAddress,
                                     registrationDate: Option[LocalDate],
                                     partOfVatGroup: Option[EncryptedValue],
                                     organisationName: Option[EncryptedValue],
                                     singleMarketIndicator: Option[EncryptedValue]
                                   )

object EncryptedVatCustomerInfo {

  implicit val format: OFormat[EncryptedVatCustomerInfo] = Json.format[EncryptedVatCustomerInfo]
}
