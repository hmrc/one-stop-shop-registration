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

import models.etmp._
import models.exclusions.ExclusionDetails
import models._
import models.requests.AmendRegistrationRequest
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

  def fromRegistrationRequest(
                               registration: Registration,
                               amendedRegistration: AmendRegistrationRequest,
                               etmpMessageType: EtmpMessageType
                             ): EtmpAmendRegistrationRequest = {

    EtmpAmendRegistrationRequest(
      administration = EtmpAdministration(etmpMessageType),
      customerIdentification = EtmpCustomerIdentification(registration.vrn),
      tradingNames = amendedRegistration.tradingNames.map(EtmpTradingNames(_)),
      schemeDetails = schemeDetails(amendedRegistration),
      bankDetails = amendedRegistration.bankDetails,
      requestedChange = calculateRequestedChange(registration, amendedRegistration),
      exclusionDetails = mapExclusionDetails(amendedRegistration.exclusionDetails)
    )
  }

  private def schemeDetails(amendedRegistration: AmendRegistrationRequest): EtmpSchemeDetails = {
    EtmpSchemeDetails(
      commencementDate = amendedRegistration.commencementDate.format(EtmpSchemeDetails.dateFormatter),
      firstSaleDate = amendedRegistration.dateOfFirstSale.map(_.format(EtmpSchemeDetails.dateFormatter)),
      euRegistrationDetails = amendedRegistration.euRegistrations.map(registration => EtmpEuRegistrationDetails.create(registration)),
      previousEURegistrationDetails = mapPreviousRegistrations(amendedRegistration.previousRegistrations),
      onlineMarketPlace = amendedRegistration.isOnlineMarketplace,
      websites = amendedRegistration.websites.map(Website(_)),
      contactName = amendedRegistration.contactDetails.fullName,
      businessTelephoneNumber = amendedRegistration.contactDetails.telephoneNumber,
      businessEmailId = amendedRegistration.contactDetails.emailAddress,
      nonCompliantReturns = amendedRegistration.nonCompliantReturns,
      nonCompliantPayments = amendedRegistration.nonCompliantPayments
    )
  }

  private def calculateRequestedChange(registration: Registration, amendedReg: AmendRegistrationRequest): EtmpRequestedChange = {
    EtmpRequestedChange(
      tradingName = amendedReg.tradingNames != registration.tradingNames,
      fixedEstablishment = amendedReg.niPresence != registration.niPresence,
      contactDetails = amendedReg.contactDetails != registration.contactDetails,
      bankDetails = amendedReg.bankDetails != registration.bankDetails,
      reRegistration = amendedReg.rejoin.contains(true),
      exclusion = amendedReg.exclusionDetails.isDefined
    )
  }

  private def mapExclusionDetails(exclusionDetails: Option[ExclusionDetails]): Option[EtmpExclusionDetails] = {
    exclusionDetails.map { exclusion =>

      EtmpExclusionDetails(
        exclusionRequestDate = exclusion.exclusionRequestDate,
        exclusionReason = exclusion.exclusionReason,
        movePOBDate = exclusion.movePOBDate,
        issuedBy = exclusion.issuedBy,
        vatNumber = exclusion.vatNumber
      )
    }
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

