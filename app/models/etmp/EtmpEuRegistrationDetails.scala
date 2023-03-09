/*
 * Copyright 2023 HM Revenue & Customs
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

import models._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, OWrites, Reads, __}

case class EtmpEuRegistrationDetails(
                                      countryOfRegistration: String,
                                      vatNumber: Option[String] = None,
                                      taxIdentificationNumber: Option[String] = None,
                                      fixedEstablishment: Option[Boolean] = None,
                                      tradingName: Option[String] = None,
                                      fixedEstablishmentAddressLine1: Option[String] = None,
                                      fixedEstablishmentAddressLine2: Option[String] = None,
                                      townOrCity: Option[String] = None,
                                      regionOrState: Option[String] = None,
                                      postcode: Option[String] = None
                                    ) {

}

object EtmpEuRegistrationDetails {

  def create(euTaxRegistration: EuTaxRegistration): EtmpEuRegistrationDetails = {
    euTaxRegistration match {
      case euVatRegistration: EuVatRegistration =>
        val vatRegistrationNumber = CountryWithValidationDetails.convertTaxIdentifierForTransfer(euVatRegistration.vatNumber, euVatRegistration.country.code)
        EtmpEuRegistrationDetails(euVatRegistration.country.code, Some(vatRegistrationNumber))
      case registrationWithFE: RegistrationWithFixedEstablishment =>
        val registrationNumber = CountryWithValidationDetails.convertTaxIdentifierForTransfer(registrationWithFE.taxIdentifier.value, registrationWithFE.country.code)
        EtmpEuRegistrationDetails(
          countryOfRegistration = registrationWithFE.country.code,
          if (registrationWithFE.taxIdentifier.identifierType == EuTaxIdentifierType.Vat) {
            Some(registrationNumber)
          } else {
            None
          },
          taxIdentificationNumber = if (registrationWithFE.taxIdentifier.identifierType == EuTaxIdentifierType.Other) {
            Some(registrationNumber)
          } else {
            None
          },
          fixedEstablishment = Some(true),
          tradingName = Some(registrationWithFE.fixedEstablishment.tradingName),
          fixedEstablishmentAddressLine1 = Some(registrationWithFE.fixedEstablishment.address.line1),
          fixedEstablishmentAddressLine2 = registrationWithFE.fixedEstablishment.address.line2,
          townOrCity = Some(registrationWithFE.fixedEstablishment.address.townOrCity),
          regionOrState = registrationWithFE.fixedEstablishment.address.stateOrRegion,
          postcode = registrationWithFE.fixedEstablishment.address.postCode
        )
      case registrationWithoutFEWithTradeDetails: RegistrationWithoutFixedEstablishmentWithTradeDetails =>
        val registrationNumber = CountryWithValidationDetails.convertTaxIdentifierForTransfer(registrationWithoutFEWithTradeDetails.taxIdentifier.value, registrationWithoutFEWithTradeDetails.country.code)
        EtmpEuRegistrationDetails(
          countryOfRegistration = registrationWithoutFEWithTradeDetails.country.code,
          if (registrationWithoutFEWithTradeDetails.taxIdentifier.identifierType == EuTaxIdentifierType.Vat) {
            Some(registrationNumber)
          } else {
            None
          },
          taxIdentificationNumber = if (registrationWithoutFEWithTradeDetails.taxIdentifier.identifierType == EuTaxIdentifierType.Other) {
            Some(registrationNumber)
          } else {
            None
          },
          fixedEstablishment = Some(false),
          tradingName = Some(registrationWithoutFEWithTradeDetails.tradeDetails.tradingName),
          fixedEstablishmentAddressLine1 = Some(registrationWithoutFEWithTradeDetails.tradeDetails.address.line1),
          fixedEstablishmentAddressLine2 = registrationWithoutFEWithTradeDetails.tradeDetails.address.line2,
          townOrCity = Some(registrationWithoutFEWithTradeDetails.tradeDetails.address.townOrCity),
          regionOrState = registrationWithoutFEWithTradeDetails.tradeDetails.address.stateOrRegion,
          postcode = registrationWithoutFEWithTradeDetails.tradeDetails.address.postCode
        )
      case registrationWithoutTaxId: RegistrationWithoutTaxId =>
        EtmpEuRegistrationDetails(
          countryOfRegistration = registrationWithoutTaxId.country.code
        )
    }
  }

  private def fromDisplayRegistrationPayload(
                                              countryOfRegistration: String,
                                              vatNumber: Option[String],
                                              taxIdentificationNumber: Option[String],
                                              fixedEstablishment: Option[Boolean],
                                              tradingName: Option[String],
                                              fixedEstablishmentAddressLine1: Option[String],
                                              fixedEstablishmentAddressLine2: Option[String],
                                              townOrCity: Option[String],
                                              regionOrState: Option[String],
                                              postcode: Option[String]
                                            ): EtmpEuRegistrationDetails =
    EtmpEuRegistrationDetails(
      countryOfRegistration = countryOfRegistration,
      vatNumber = vatNumber,
      taxIdentificationNumber = taxIdentificationNumber,
      fixedEstablishment = fixedEstablishment,
      tradingName = tradingName,
      fixedEstablishmentAddressLine1 = fixedEstablishmentAddressLine1,
      fixedEstablishmentAddressLine2 = fixedEstablishmentAddressLine2,
      townOrCity = townOrCity,
      regionOrState = regionOrState,
      postcode = postcode
    )

  implicit val reads: Reads[EtmpEuRegistrationDetails] =
    (
      (__ \ "issuedBy").read[String] and
        (__ \ "vatNumber").readNullable[String] and
        (__ \ "taxIdentificationNumber").readNullable[String] and
        (__ \ "fixedEstablishment").readNullable[Boolean] and
        (__ \ "fixedEstablishmentTradingName").readNullable[String] and
        (__ \ "fixedEstablishmentAddressLine1").readNullable[String] and
        (__ \ "fixedEstablishmentAddressLine2").readNullable[String] and
        (__ \ "townOrCity").readNullable[String] and
        (__ \ "regionOrState").readNullable[String] and
        (__ \ "postcode").readNullable[String]
      ) (EtmpEuRegistrationDetails.fromDisplayRegistrationPayload _)

  implicit val writes: OWrites[EtmpEuRegistrationDetails] =
    Json.writes[EtmpEuRegistrationDetails]
}

