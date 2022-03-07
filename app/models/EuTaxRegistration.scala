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
import play.api.libs.json.{Json, OFormat, Reads, Writes}

sealed trait EuTaxRegistration

object EuTaxRegistration {

  implicit val reads: Reads[EuTaxRegistration] =
    RegistrationWithFixedEstablishment.format.widen[EuTaxRegistration] orElse
      EuVatRegistration.format.widen[EuTaxRegistration] orElse
      RegistrationWithoutFixedEstablishment.format.widen[EuTaxRegistration]


  implicit val writes: Writes[EuTaxRegistration] = Writes {
    case v: EuVatRegistration                     => Json.toJson(v)(EuVatRegistration.format)
    case fe: RegistrationWithFixedEstablishment   => Json.toJson(fe)(RegistrationWithFixedEstablishment.format)
    case w: RegistrationWithoutFixedEstablishment => Json.toJson(w)(RegistrationWithoutFixedEstablishment.format)
  }
}

sealed trait EncryptedEuTaxRegistration

object EncryptedEuTaxRegistration {

  implicit val reads: Reads[EncryptedEuTaxRegistration] =
    EncryptedRegistrationWithFixedEstablishment.format.widen[EncryptedEuTaxRegistration] orElse
      EncryptedEuVatRegistration.format.widen[EncryptedEuTaxRegistration] orElse
      EncryptedRegistrationWithoutFixedEstablishment.format.widen[EncryptedEuTaxRegistration]

  implicit val writes: Writes[EncryptedEuTaxRegistration] = Writes {
    case v: EncryptedEuVatRegistration                  => Json.toJson(v)(EncryptedEuVatRegistration.format)
    case f: EncryptedRegistrationWithFixedEstablishment => Json.toJson(f)(EncryptedRegistrationWithFixedEstablishment.format)
    case w: EncryptedRegistrationWithoutFixedEstablishment => Json.toJson(w)(EncryptedRegistrationWithoutFixedEstablishment.format)
  }
}

final case class EuVatRegistration(
                                    country: Country,
                                    taxIdentifier: EuTaxIdentifier
                                  ) extends EuTaxRegistration

object EuVatRegistration {

  implicit val format: OFormat[EuVatRegistration] =
    Json.format[EuVatRegistration]
}

final case class EncryptedEuVatRegistration(
                                             country: EncryptedCountry,
                                             taxIdentifier: EncryptedEuTaxIdentifier
                                           ) extends EncryptedEuTaxRegistration

object EncryptedEuVatRegistration {

  implicit val format: OFormat[EncryptedEuVatRegistration] = Json.format[EncryptedEuVatRegistration]
}

final case class RegistrationWithFixedEstablishment(
                                                     country: Country,
                                                     taxIdentifier: EuTaxIdentifier,
                                                     fixedEstablishment: FixedEstablishment
                                                   ) extends EuTaxRegistration

object RegistrationWithFixedEstablishment {
  implicit val format: OFormat[RegistrationWithFixedEstablishment] =
    Json.format[RegistrationWithFixedEstablishment]
}

final case class EncryptedRegistrationWithFixedEstablishment(
                                                              country: EncryptedCountry,
                                                              taxIdentifier: EncryptedEuTaxIdentifier,
                                                              fixedEstablishment: EncryptedFixedEstablishment
                                                            ) extends EncryptedEuTaxRegistration

object EncryptedRegistrationWithFixedEstablishment {

  implicit val format: OFormat[EncryptedRegistrationWithFixedEstablishment] =
    Json.format[EncryptedRegistrationWithFixedEstablishment]
}

final case class RegistrationWithoutFixedEstablishment(country: Country) extends EuTaxRegistration

object RegistrationWithoutFixedEstablishment {
  implicit val format: OFormat[RegistrationWithoutFixedEstablishment] =
    Json.format[RegistrationWithoutFixedEstablishment]
}

final case class EncryptedRegistrationWithoutFixedEstablishment(country: EncryptedCountry) extends EncryptedEuTaxRegistration

object EncryptedRegistrationWithoutFixedEstablishment {
  implicit val format: OFormat[EncryptedRegistrationWithoutFixedEstablishment] =
    Json.format[EncryptedRegistrationWithoutFixedEstablishment]
}
