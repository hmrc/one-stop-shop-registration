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

import config.Constants.quarantineReason4
import logging.Logging
import models._
import models.etmp.EtmpExclusionReason.{CeasedTrade, FailsToComply, NoLongerMeetsConditions, NoLongerSupplies, Reversal, TransferringMSID, VoluntarilyLeaves}
import models.etmp.EtmpSchemeDetails.dateFormatter
import models.exclusions.ExcludedTrader
import models.requests.RegistrationRequest
import play.api.libs.json.{Json, OFormat}

case class EtmpRegistrationRequest(
                                    administration: EtmpAdministration,
                                    customerIdentification: EtmpCustomerIdentification,
                                    tradingNames: Seq[EtmpTradingNames],
                                    schemeDetails: EtmpSchemeDetails,
                                    bankDetails: BankDetails
                                  )

object EtmpRegistrationRequest extends Logging {

  def fromRegistrationRequest(registration: RegistrationRequest, etmpMessageType: EtmpMessageType): EtmpRegistrationRequest = {
    EtmpRegistrationRequest(
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
        nonCompliantPayments = registration.nonCompliantPayments,
        exclusions = registration.excludedTrader.map(excludedTrader => mapEtmpExclusion(excludedTrader)).toSeq
      ),
      bankDetails = registration.bankDetails
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

  private def mapEtmpExclusion(excludedTrader: ExcludedTrader): EtmpExclusion = {
    excludedTrader.effectiveDate match {
      case Some(effectiveDate) =>
        EtmpExclusion(
          exclusionReason = mapEtmpExclusionReason(excludedTrader.exclusionReason),
          effectiveDate = effectiveDate,
          decisionDate = excludedTrader.effectivePeriod.lastDay, // TODO -> Check this
          quarantine = excludedTrader.exclusionReason == quarantineReason4
        )
      case _ =>
        val message: String = "Unable to parse effective date"
        logger.error(message)
        throw new Exception(message)
    }
  }

  private def mapEtmpExclusionReason(exclusionReason: Int): EtmpExclusionReason = {
    EtmpExclusionReason.enumerable.withName(exclusionReason.toString) match {
      case Some(etmpExclusionReason) => etmpExclusionReason
      case _ =>
        val message: String = "Not a valid exclusion reason"
        logger.error(message)
        throw new IllegalStateException(message)
    }
  }

  implicit val format: OFormat[EtmpRegistrationRequest] = Json.format[EtmpRegistrationRequest]
}

