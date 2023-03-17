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

package models

import crypto.EncryptedValue
import logging.Logging
import models.des.VatCustomerInfo
import models.etmp.EtmpSchemeDetails.dateFormatter
import models.etmp._
import models.exclusions.ExcludedTrader
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.domain.Vrn

import java.time.{Instant, LocalDate}

case class Registration(
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
                         submissionReceived: Option[Instant],
                         lastUpdated: Option[Instant],
                         excludedTrader: Option[ExcludedTrader] = None,
                         nonCompliantReturns: Option[Int] = None,
                         nonCompliantPayments: Option[Int] = None
                       )

object Registration extends Logging {
  def fromEtmpRegistration(
                            vrn: Vrn,
                            vatDetails: VatCustomerInfo,
                            tradingNames: Seq[EtmpTradingNames],
                            schemeDetails: EtmpSchemeDetails,
                            bankDetails: BankDetails
                          ): Registration =
    Registration(
      vrn = vrn,
      registeredCompanyName = vatDetails.organisationName.getOrElse {
        logger.warn("Registration did not contain a registered company name")
        throw new IllegalStateException("Registration must have a registered company name")
      },
      tradingNames = tradingNames.map(_.tradingName),
      vatDetails = VatDetails(
        vatDetails.registrationDate.getOrElse {
          logger.warn("Registration did not contain a registration date")
          throw new IllegalStateException("Registration must have a registration date")
        },
        vatDetails.address,
        vatDetails.partOfVatGroup,
        VatDetailSource.Etmp
      ),
      euRegistrations = convertToEuTaxRegistration(schemeDetails.euRegistrationDetails),
      contactDetails = ContactDetails(
        fullName = schemeDetails.contactName,
        telephoneNumber = schemeDetails.businessTelephoneNumber,
        emailAddress = schemeDetails.businessEmailId),
      websites = schemeDetails.websites.map(_.websiteAddress),
      commencementDate = LocalDate.parse(schemeDetails.commencementDate, dateFormatter),
      previousRegistrations = convertToPreviousRegistration(schemeDetails.previousEURegistrationDetails),
      bankDetails = bankDetails,
      isOnlineMarketplace = schemeDetails.onlineMarketPlace,
      niPresence = None,
      dateOfFirstSale = schemeDetails.firstSaleDate.map(date => LocalDate.parse(date, dateFormatter)),
      submissionReceived = None,
      lastUpdated = None,
      nonCompliantReturns = schemeDetails.nonCompliantReturns,
      nonCompliantPayments = schemeDetails.nonCompliantPayments
    )

  private def convertToEuTaxRegistration(etmpEuRegistrationDetails: Seq[EtmpEuRegistrationDetails]): Seq[EuTaxRegistration] = {

    for {
      euRegistration <- etmpEuRegistrationDetails
    } yield {

      val country = getCountry(euRegistration.countryOfRegistration)

      euRegistration.fixedEstablishment match {
        case Some(true) => // Fixed Establishment
          RegistrationWithFixedEstablishment(
            country = country,
            taxIdentifier = determineTaxIdentifier(euRegistration),
            fixedEstablishment = getTradeDetails(euRegistration)
          )

        case Some(false) => // Sends Goods
          RegistrationWithoutFixedEstablishmentWithTradeDetails(
            country = country,
            taxIdentifier = determineTaxIdentifier(euRegistration),
            tradeDetails = getTradeDetails(euRegistration)
          )
        case _ => // Other MS
          (euRegistration.vatNumber, euRegistration.taxIdentificationNumber) match {
            case (Some(vatNumber), None) =>
              EuVatRegistration(
                country = country,
                vatNumber = vatNumber
              )
            case (None, None) =>
              RegistrationWithoutTaxId(
                country = country
              )
            case (_, _) =>
              logger.warn("A tax Identification Number was retrieved")
              throw new IllegalStateException("An other Member State cannot have a Tax Identification Number")
          }
      }
    }
  }

  private def convertToPreviousRegistration(previousEURegistrationDetails: Seq[EtmpPreviousEURegistrationDetails]): Seq[PreviousRegistration] = {
    for {
      issuedBy <- previousEURegistrationDetails.map(_.issuedBy).distinct
    } yield {

      val country = getCountry(issuedBy)

      val schemeDetailsForCountry = previousEURegistrationDetails.filter(_.issuedBy == issuedBy)

      PreviousRegistrationNew(
        country = country,
        previousSchemesDetails = schemeDetailsForCountry.map(convertPreviousSchemeDetails)
      )
    }
  }

  private def convertPreviousSchemeDetails(etmpPreviousEURegistrationDetails: EtmpPreviousEURegistrationDetails): PreviousSchemeDetails = {
    PreviousSchemeDetails(
      previousScheme = convertPreviousScheme(etmpPreviousEURegistrationDetails.schemeType),
      previousSchemeNumbers = PreviousSchemeNumbers(
        previousSchemeNumber = etmpPreviousEURegistrationDetails.registrationNumber,
        previousIntermediaryNumber = etmpPreviousEURegistrationDetails.intermediaryNumber
      )
    )
  }

  private def convertPreviousScheme(schemeType: SchemeType): PreviousScheme = {
    schemeType match {
      case SchemeType.OSSUnion => PreviousScheme.OSSU
      case SchemeType.OSSNonUnion => PreviousScheme.OSSNU
      case SchemeType.IOSSWithoutIntermediary => PreviousScheme.IOSSWOI
      case SchemeType.IOSSWithIntermediary => PreviousScheme.IOSSWI
      case _ => throw new Exception("Unknown scheme, unable to convert")
    }
  }

  private def determineTaxIdentifier(etmpEuRegistrationDetails: EtmpEuRegistrationDetails): EuTaxIdentifier = {
    if (etmpEuRegistrationDetails.vatNumber.nonEmpty) {
      EuTaxIdentifier(EuTaxIdentifierType.Vat, getTaxId(etmpEuRegistrationDetails.vatNumber, None))

    } else {
      EuTaxIdentifier(EuTaxIdentifierType.Other, getTaxId(None, etmpEuRegistrationDetails.taxIdentificationNumber))
    }
  }

  private def getTaxId(vatNumber: Option[String], taxIdentificationNumber: Option[String]): String = {
    (vatNumber, taxIdentificationNumber) match {
      case (Some(vatNumber), None) =>
        vatNumber
      case (None, Some(taxIdentificationNumber)) =>
        taxIdentificationNumber
      case _ =>
        logger.warn("No Tax Identification Number was retrieved")
        throw new IllegalStateException("Fixed establishment and send goods states must have a Tax Identification Number")
    }
  }

  private def getCountry(countryCode: String): Country = {
    val countryName = Country
      .euCountries.find(_.code == countryCode).get.name
    Country(code = countryCode, name = countryName)
  }

  private def getTradeDetails(etmpEuRegistrationDetails: EtmpEuRegistrationDetails): TradeDetails = {
    TradeDetails(
      tradingName = etmpEuRegistrationDetails.tradingName.getOrElse(""),
      address = InternationalAddress(
        line1 = etmpEuRegistrationDetails.fixedEstablishmentAddressLine1.getOrElse(""),
        line2 = etmpEuRegistrationDetails.fixedEstablishmentAddressLine2,
        townOrCity = etmpEuRegistrationDetails.townOrCity.getOrElse(""),
        stateOrRegion = etmpEuRegistrationDetails.regionOrState,
        postCode = etmpEuRegistrationDetails.postcode,
        country = getCountry(etmpEuRegistrationDetails.countryOfRegistration)
      )
    )
  }

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
                                  commencementDate: LocalDate,
                                  previousRegistrations: Seq[EncryptedPreviousRegistration],
                                  bankDetails: EncryptedBankDetails,
                                  isOnlineMarketplace: EncryptedValue,
                                  niPresence: Option[NiPresence],
                                  submissionReceived: Option[Instant],
                                  lastUpdated: Option[Instant],
                                  dateOfFirstSale: Option[LocalDate],
                                  nonCompliantReturns: Option[Int],
                                  nonCompliantPayments: Option[Int]
                                )

object EncryptedRegistration {

  implicit val format: OFormat[EncryptedRegistration] = Json.format[EncryptedRegistration]
}