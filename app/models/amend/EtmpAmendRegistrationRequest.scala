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

package models.amend

import models.etmp.{EtmpAdministration, EtmpCustomerIdentification, EtmpEuRegistrationDetails, EtmpMessageType, EtmpPreviousEURegistrationDetails, EtmpRegistrationRequest, EtmpSchemeDetails, EtmpTradingNames, SchemeType, Website}
import models.{BankDetails, CountryWithValidationDetails, PreviousRegistration, PreviousRegistrationLegacy, PreviousRegistrationNew, PreviousScheme}
import models.requests.RegistrationRequest
import play.api.libs.json.{Json, OFormat}

case class EtmpAmendRegistrationRequest(
                             administration: EtmpAdministration,
                             customerIdentification: EtmpCustomerIdentification,
                             tradingNames: Seq[EtmpTradingNames],
                             schemeDetails: EtmpSchemeDetails,
                             bankDetails: BankDetails,
                             requestedChange: EtmpRequestedChange,
                             exclusionDetails: Option[EtmpExclusionDetails]
                           ) {
}

object EtmpAmendRegistrationRequest {

  def fromRegistrationRequest(registration: RegistrationRequest, etmpMessageType: EtmpMessageType): EtmpAmendRegistrationRequest = {
    EtmpAmendRegistrationRequest(
      administration = EtmpAdministration(etmpMessageType),
      customerIdentification = EtmpCustomerIdentification(registration.vrn),
      tradingNames = registration.tradingNames.map(EtmpTradingNames(_)),
      schemeDetails = EtmpSchemeDetails(
        commencementDate = registration.commencementDate.format(EtmpSchemeDetails.dateFormatter),
        firstSaleDate = registration.dateOfFirstSale.map(_.format(EtmpSchemeDetails.dateFormatter)),
        euRegistrationDetails = registration.euRegistrations.map(registration => EtmpEuRegistrationDetails.create(registration)),
        previousEURegistrationDetails = mapPreviousRegistrations(registration.previousRegistrations),
        onlineMarketPlace = registration.isOnlineMarketplace,
        websites = registration.websites.map(Website(_)),
        contactName = registration.contactDetails.fullName,
        businessTelephoneNumber = registration.contactDetails.telephoneNumber,
        businessEmailId = registration.contactDetails.emailAddress,
        nonCompliantReturns = registration.nonCompliantReturns,
        nonCompliantPayments = registration.nonCompliantPayments
      ),
      bankDetails = registration.bankDetails,
      requestedChange = EtmpRequestedChange( // TODO calculate diffs
        tradingName = false,
        fixedEstablishment = false,
        contactDetails = false,
        bankDetails = false,
        reRegistration = false,
        exclusion = false
      ),
      exclusionDetails = None // TODO map from exclusion
    )
  }

  private def mapPreviousRegistrations(previousRegistrations: Seq[PreviousRegistration]): Seq[EtmpPreviousEURegistrationDetails] = {

    previousRegistrations.flatMap {
      case newPreviousRegistrations: PreviousRegistrationNew =>
        newPreviousRegistrations.previousSchemesDetails.map { previousSchemeDetails =>

          val registrationNumber = previousSchemeDetails.previousScheme match {
            case PreviousScheme.OSSU => CountryWithValidationDetails.convertTaxIdentifierForTransfer(previousSchemeDetails.previousSchemeNumbers.previousSchemeNumber, newPreviousRegistrations.country.code)
            case _ => previousSchemeDetails.previousSchemeNumbers.previousSchemeNumber
          }

          EtmpPreviousEURegistrationDetails(
            issuedBy = newPreviousRegistrations.country.code,
            registrationNumber = registrationNumber,
            schemeType = convertSchemeType(previousSchemeDetails.previousScheme),
            intermediaryNumber = previousSchemeDetails.previousSchemeNumbers.previousIntermediaryNumber
          )
        }

      case legacyPreviousRegistrations: PreviousRegistrationLegacy =>

        val registrationNumber = legacyPreviousRegistrations.vatNumber

        Seq(EtmpPreviousEURegistrationDetails(
          issuedBy = legacyPreviousRegistrations.country.code,
          registrationNumber = registrationNumber,
          schemeType = SchemeType.OSSNonUnion,
          None
        ))
    }
  }

  private def convertSchemeType(previousScheme: PreviousScheme): SchemeType = {
    previousScheme match {
      case PreviousScheme.OSSU => SchemeType.OSSUnion
      case PreviousScheme.OSSNU => SchemeType.OSSNonUnion
      case PreviousScheme.IOSSWOI => SchemeType.IOSSWithoutIntermediary
      case PreviousScheme.IOSSWI => SchemeType.IOSSWithIntermediary
      case _ => throw new Exception("Unknown scheme, unable to convert")
    }
  }

  implicit val format: OFormat[EtmpAmendRegistrationRequest] = Json.format[EtmpAmendRegistrationRequest]
}

