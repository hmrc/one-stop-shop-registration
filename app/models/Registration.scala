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

package models

import logging.Logging
import models.des.VatCustomerInfo
import models.etmp.EtmpSchemeDetails.dateFormatter
import models.etmp.*
import models.exclusions.ExcludedTrader
import models.exclusions.ExcludedTrader.fromEtmpExclusion
import play.api.libs.json.{JsObject, JsValue, Json, OFormat, OWrites, Reads, Writes}
import uk.gov.hmrc.domain.Vrn

import java.time.{Instant, LocalDate, ZoneId}

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
                         transferringMsidEffectiveFromDate: Option[LocalDate] = None,
                         nonCompliantReturns: Option[String] = None,
                         nonCompliantPayments: Option[String] = None,
                         unusableStatus: Option[Boolean] = None,
                         adminUse: AdminUse
                       )

object Registration extends Logging {

  def fromEtmpRegistration(
                            vrn: Vrn,
                            vatDetails: VatCustomerInfo,
                            tradingNames: Seq[EtmpTradingNames],
                            schemeDetails: EtmpDisplaySchemeDetails,
                            bankDetails: BankDetails,
                            adminUse: AdminUse
                          ): Registration = {
    Registration(
      vrn = vrn,
      registeredCompanyName = vatDetails.organisationName.getOrElse {
        vatDetails.individualName.getOrElse{
          logger.warn("Registration did not contain a registered company name or individual name")
          throw new IllegalStateException("Registration must have a registered company name or individual name")
        }
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
      submissionReceived = schemeDetails.registrationDate.map(registrationDate => LocalDate.parse(registrationDate, dateFormatter).atStartOfDay(ZoneId.systemDefault()).toInstant),
      lastUpdated = None,
      nonCompliantReturns = schemeDetails.nonCompliantReturns,
      nonCompliantPayments = schemeDetails.nonCompliantPayments,
      excludedTrader = schemeDetails.exclusions.headOption.map(etmpExclusion => fromEtmpExclusion(vrn, etmpExclusion)),
      unusableStatus = schemeDetails.unusableStatus,
      adminUse = adminUse
    )
  }

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
    Country
      .euCountries.find(_.code == countryCode) match {
      case Some(country) => country
      case _ =>
        val exception = new IllegalStateException(s"Unable to find country $countryCode")
        logger.error(exception.getMessage, exception)
        throw exception
    }
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
                                  registeredCompanyName: String,
                                  tradingNames: Seq[String],
                                  vatDetails: EncryptedVatDetails,
                                  euRegistrations: Seq[EncryptedEuTaxRegistration],
                                  contactDetails: EncryptedContactDetails,
                                  websites: Seq[String],
                                  commencementDate: LocalDate,
                                  previousRegistrations: Seq[EncryptedPreviousRegistration],
                                  bankDetails: EncryptedBankDetails,
                                  isOnlineMarketplace: String,
                                  niPresence: Option[NiPresence],
                                  submissionReceived: Option[Instant],
                                  lastUpdated: Option[Instant],
                                  dateOfFirstSale: Option[LocalDate],
                                  nonCompliantReturns: Option[String],
                                  nonCompliantPayments: Option[String]
                                )

object EncryptedRegistration {


  implicit val writes: OWrites[EncryptedRegistration] = new OWrites[EncryptedRegistration] {
    override def writes(o: EncryptedRegistration): JsObject = {
      Json.obj(
        "vrn" -> o.vrn.value,
        "registeredCompanyName" -> o.registeredCompanyName,
        "tradingNames" -> o.tradingNames,
        "vatDetails" -> o.vatDetails,
        "euRegistrations" -> o.euRegistrations,
        "contactDetails" -> o.contactDetails,
        "websites" -> o.websites,
        "commencementDate" -> o.commencementDate.toString,
        "previousRegistrations" -> o.previousRegistrations,
        "bankDetails" -> o.bankDetails,
        "isOnlineMarketplace" -> o.isOnlineMarketplace
      ) ++
          o.niPresence.map(value => Json.obj("niPresence" -> value.toString)).getOrElse(Json.obj()) ++
          o.submissionReceived.map(value => Json.obj("submissionReceived" -> value.toString)).getOrElse(Json.obj()) ++
          o.lastUpdated.map(value => Json.obj("lastUpdated" -> value.toString)).getOrElse(Json.obj()) ++
          o.dateOfFirstSale.map(value => Json.obj("dateOfFirstSale" -> value.toString)).getOrElse(Json.obj()) ++
          o.nonCompliantReturns.map(value => Json.obj("nonCompliantReturns" -> value)).getOrElse(Json.obj()) ++
          o.nonCompliantPayments.map(value => Json.obj("nonCompliantPayments" -> value)).getOrElse(Json.obj())
    }
  }

    implicit val reads: Reads[EncryptedRegistration] = Json.reads[EncryptedRegistration]

    implicit val format: OFormat[EncryptedRegistration] = OFormat(reads, writes)
}