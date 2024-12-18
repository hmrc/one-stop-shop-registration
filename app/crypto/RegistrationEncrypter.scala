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

package crypto

import config.AppConfig
import models._
import models.etmp.AdminUse
import services.crypto.EncryptionService
import uk.gov.hmrc.domain.Vrn

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject
import scala.util.Try

class RegistrationEncrypter @Inject()(
                                       appConfig: AppConfig,
                                       encryptionService: EncryptionService,
                                       clock: Clock
                                     ) {

  protected val key: String = appConfig.encryptionKey

  def encryptCountry(country: Country): EncryptedCountry = {
    def e(field: String): String = encryptionService.encryptField(field)

    EncryptedCountry(e(country.code), e(country.name))
  }

  def decryptCountry(country: EncryptedCountry): Country = {
    def d(field: String): String = encryptionService.decryptField(field)
    import country._

    Country(d(code), d(name))
  }

  private def encryptUkAddress(address: UkAddress): EncryptedUkAddress = {
    def e(field: String): String = encryptionService.encryptField(field)
    import address._

    EncryptedUkAddress(e(line1), line2 map e, e(townOrCity), county map e, e(postCode))
  }

  private def decryptUkAddress(address: EncryptedUkAddress): UkAddress = {
    def d(field: String): String = encryptionService.decryptField(field)
    import address._

    UkAddress(d(line1), line2 map d, d(townOrCity), county map d, d(postCode))
  }

  private def encryptInternationalAddress(address: InternationalAddress): EncryptedInternationalAddress = {
    def e(field: String): String = encryptionService.encryptField(field)
    import address._

    EncryptedInternationalAddress(e(line1), line2 map e, e(townOrCity), stateOrRegion map e, postCode map e, country)
  }

  private def decryptInternationalAddress(address: EncryptedInternationalAddress): InternationalAddress = {
    def d(field: String): String = encryptionService.decryptField(field)
    import address._

    InternationalAddress(d(line1), line2 map d, d(townOrCity), stateOrRegion map d, postCode map d, country)
  }

  private def encryptDesAddress(address: DesAddress): EncryptedDesAddress = {
    def e(field: String): String = encryptionService.encryptField(field)
    import address._

    EncryptedDesAddress(e(line1), line2 map e, line3 map e, line4 map e, line5 map e, postCode map e, e(countryCode))
  }

  private def decryptDesAddress(address: EncryptedDesAddress): DesAddress = {
    def d(field: String): String = encryptionService.decryptField(field)
    import address._

    DesAddress(d(line1), line2 map d, line3 map d, line4 map d, line5 map d, postCode map d, d(countryCode))
  }

  def encryptAddress(address: Address): EncryptedAddress = address match {
    case u: UkAddress            => encryptUkAddress(u)
    case i: InternationalAddress => encryptInternationalAddress(i)
    case d: DesAddress           => encryptDesAddress(d)
  }

  def decryptAddress(address: EncryptedAddress): Address = address match {
    case u: EncryptedUkAddress            => decryptUkAddress(u)
    case i: EncryptedInternationalAddress => decryptInternationalAddress(i)
    case d: EncryptedDesAddress           => decryptDesAddress(d)
  }

  def encryptBankDetails(bankDetails: BankDetails): EncryptedBankDetails = {
    def e(field: String): String = encryptionService.encryptField(field)
    import bankDetails._

    EncryptedBankDetails(e(accountName), bic.map(b => e(b.toString)), e(iban.toString))
  }

  def decryptBankDetails(bankDetails: EncryptedBankDetails): BankDetails = {
    def d(field: String): String = encryptionService.decryptField(field)
    import bankDetails._

    val b = bic.map(x => Bic(d(x)).getOrElse(throw new EncryptionDecryptionException("decryptBankDetails", "Unable to decrypt BIC", "")))
    val i = Iban(d(iban)).toOption.getOrElse(throw new EncryptionDecryptionException("decryptBankDetails", "Unable to decrypt IBAN", ""))

    BankDetails(d(accountName), b, i)
  }

  def encryptContactDetails(contactDetails: ContactDetails): EncryptedContactDetails = {
    def e(field: String): String = encryptionService.encryptField(field)
    import contactDetails._

    EncryptedContactDetails(e(fullName), e(telephoneNumber), e(emailAddress))
  }

  def decryptContactDetails(contactDetails: EncryptedContactDetails): ContactDetails = {
    def d(field: String): String = encryptionService.decryptField(field)
    import contactDetails._

    ContactDetails(d(fullName), d(telephoneNumber), d(emailAddress))
  }

  def encryptEuTaxIdentifier(identifier: EuTaxIdentifier): EncryptedEuTaxIdentifier = {
    def e(field: String): String = encryptionService.encryptField(field)
    import identifier._

    EncryptedEuTaxIdentifier(e(identifierType.toString), e(value))
  }

  def decryptEuTaxIdentifier(identifier: EncryptedEuTaxIdentifier): EuTaxIdentifier = {
    def d(field: String): String = encryptionService.decryptField(field)

    import models.EuTaxIdentifierType._

    def decryptIdentifierType(field: String): EuTaxIdentifierType = {
      EuTaxIdentifierType.withName(d(field)).getOrElse(throw new Exception("Unable to decrypt value"))
    }

    import identifier._

    EuTaxIdentifier(decryptIdentifierType(identifierType), d(value))
  }

  private def encryptEuVatRegistration(registration: EuVatRegistration): EncryptedEuVatRegistration = {
    def e(field: String): String = encryptionService.encryptField(field)
    import registration._

    EncryptedEuVatRegistration(encryptCountry(country), e(vatNumber))
  }

  private def decryptEuVatRegistration(registration: EncryptedEuVatRegistration): EuVatRegistration = {
    def d(field: String): String = encryptionService.decryptField(field)
    import registration._

    EuVatRegistration(decryptCountry(country), d(vatNumber))
  }

  private def encryptRegistrationWithoutFixedEstablishmentWithTradeDetails(registration: RegistrationWithoutFixedEstablishmentWithTradeDetails): EncryptedRegistrationWithoutFixedEstablishmentWithTradeDetails = {
    import registration._

    EncryptedRegistrationWithoutFixedEstablishmentWithTradeDetails(
      encryptCountry(country),
      encryptEuTaxIdentifier(taxIdentifier),
      encryptTradeDetails(tradeDetails)
    )
  }

  private def decryptRegistrationWithoutFixedEstablishmentWithTradeDetails(registration: EncryptedRegistrationWithoutFixedEstablishmentWithTradeDetails): RegistrationWithoutFixedEstablishmentWithTradeDetails = {
    import registration._

    RegistrationWithoutFixedEstablishmentWithTradeDetails(
      decryptCountry(country),
      decryptEuTaxIdentifier(taxIdentifier),
      decryptTradeDetails(tradeDetails)
    )
  }

  private def encryptRegistrationWithFixedEstablishment(registration: RegistrationWithFixedEstablishment): EncryptedRegistrationWithFixedEstablishment = {
    import registration._

    EncryptedRegistrationWithFixedEstablishment(
      encryptCountry(country),
      encryptEuTaxIdentifier(taxIdentifier),
      encryptTradeDetails(fixedEstablishment)
    )
  }

  private def decryptRegistrationWithFixedEstablishment(registration: EncryptedRegistrationWithFixedEstablishment): RegistrationWithFixedEstablishment = {
    import registration._

    RegistrationWithFixedEstablishment(
      decryptCountry(country),
      decryptEuTaxIdentifier(taxIdentifier),
      decryptTradeDetails(fixedEstablishment)
    )
  }

  private def encryptRegistrationWithoutTaxId(registration: RegistrationWithoutTaxId) : EncryptedRegistrationWithoutTaxId =
    EncryptedRegistrationWithoutTaxId(encryptCountry(registration.country))

  private def decryptRegistrationWithoutTaxId(registration: EncryptedRegistrationWithoutTaxId) : RegistrationWithoutTaxId =
    RegistrationWithoutTaxId(decryptCountry(registration.country))

  def encryptEuTaxRegistration(registration: EuTaxRegistration): EncryptedEuTaxRegistration = {
    registration match {
    case v: EuVatRegistration => encryptEuVatRegistration (v)
    case wf: RegistrationWithoutFixedEstablishmentWithTradeDetails => encryptRegistrationWithoutFixedEstablishmentWithTradeDetails (wf)
    case f: RegistrationWithFixedEstablishment => encryptRegistrationWithFixedEstablishment (f)
    case w: RegistrationWithoutTaxId => encryptRegistrationWithoutTaxId (w)
  }
  }

  def decryptEuTaxRegistration(registration: EncryptedEuTaxRegistration): EuTaxRegistration =
    registration match {
      case v: EncryptedEuVatRegistration                    => decryptEuVatRegistration(v)
      case wf: EncryptedRegistrationWithoutFixedEstablishmentWithTradeDetails              => decryptRegistrationWithoutFixedEstablishmentWithTradeDetails(wf)
      case f: EncryptedRegistrationWithFixedEstablishment    => decryptRegistrationWithFixedEstablishment(f)
      case w: EncryptedRegistrationWithoutTaxId => decryptRegistrationWithoutTaxId(w)
    }

  private def encryptTradeDetails(fixedEstablishment: TradeDetails): EncryptedTradeDetails = {
    def e(field: String): String = encryptionService.encryptField(field)
    import fixedEstablishment._

    EncryptedTradeDetails(e(tradingName), encryptInternationalAddress(address))
  }

  private def decryptTradeDetails(fixedEstablishment: EncryptedTradeDetails): TradeDetails = {
    def d(field: String): String = encryptionService.decryptField(field)
    import fixedEstablishment._

    TradeDetails(d(tradingName), decryptInternationalAddress(address))
  }

  def encryptPreviousRegistration(registration: PreviousRegistration): EncryptedPreviousRegistration =
    registration match {
      case ep: PreviousRegistrationNew => encryptPreviousRegistrationNew(ep)
      case lp: PreviousRegistrationLegacy => encryptPreviousRegistrationLegacy(lp)
    }

  def decryptPreviousRegistration(registration: EncryptedPreviousRegistration): PreviousRegistration =
    registration match {
      case p: EncryptedPreviousRegistrationNew => decryptPreviousRegistrationNew(p)
      case l: EncryptedPreviousRegistrationLegacy => decryptPreviousRegistrationLegacy(l)
    }

  private def encryptPreviousRegistrationNew(registration: PreviousRegistrationNew): EncryptedPreviousRegistrationNew = {
    import registration._

    EncryptedPreviousRegistrationNew(encryptCountry(country), previousSchemesDetails.map(encryptPreviousSchemeDetails))
  }

  private def decryptPreviousRegistrationNew(registration: EncryptedPreviousRegistrationNew): PreviousRegistrationNew = {
    import registration._

    PreviousRegistrationNew(decryptCountry(country), previousSchemeDetails.map(decryptPreviousSchemeDetails))
  }

  private def encryptPreviousRegistrationLegacy(registration: PreviousRegistrationLegacy): EncryptedPreviousRegistrationLegacy = {
    def e(field: String): String = encryptionService.encryptField(field)
    import registration._

    EncryptedPreviousRegistrationLegacy(encryptCountry(country), e(vatNumber))
  }

  private def decryptPreviousRegistrationLegacy(registration: EncryptedPreviousRegistrationLegacy): PreviousRegistrationLegacy = {
    def d(field: String): String = encryptionService.decryptField(field)
    import registration._

    PreviousRegistrationLegacy(decryptCountry(country), d(vatNumber))
  }

  def encryptPreviousSchemeDetails(previousSchemeDetails: PreviousSchemeDetails): EncryptedPreviousSchemeDetails = {
    def e(field: String): String = encryptionService.encryptField(field)
    import previousSchemeDetails._

    EncryptedPreviousSchemeDetails(e(previousScheme.toString), encryptPreviousSchemeNumbers(previousSchemeNumbers))
  }

  def decryptPreviousSchemeDetails(previousSchemeDetails: EncryptedPreviousSchemeDetails): PreviousSchemeDetails = {
    def d(field: String): String = encryptionService.decryptField(field)
    import models.PreviousScheme._
    import previousSchemeDetails._

    def decryptPreviousScheme(field: String): PreviousScheme =
      PreviousScheme.withName(d(field)).getOrElse(throw new Exception("Unable to decrypt value"))

    PreviousSchemeDetails(decryptPreviousScheme(previousScheme), decryptPreviousSchemeNumbers(previousSchemeNumbers))
  }

  def encryptPreviousSchemeNumbers(previousSchemeNumbers: PreviousSchemeNumbers): EncryptedPreviousSchemeNumbers = {
    def e(field: String): String = encryptionService.encryptField(field)
    import previousSchemeNumbers._

    EncryptedPreviousSchemeNumbers(e(previousSchemeNumber), previousIntermediaryNumber.map(e))
  }

  def decryptPreviousSchemeNumbers(previousSchemeNumbers: EncryptedPreviousSchemeNumbers): PreviousSchemeNumbers = {
    def d(field: String): String = encryptionService.decryptField(field)
    import previousSchemeNumbers._

    PreviousSchemeNumbers(d(previousSchemeNumber), previousIntermediaryNumber.map(d))
  }

  def encryptVatDetails(vatDetails: VatDetails): EncryptedVatDetails = {
    def e(field: String): String = encryptionService.encryptField(field)
    import vatDetails._

    EncryptedVatDetails(registrationDate, encryptAddress(address), e(partOfVatGroup.toString), source)
  }

  def decryptVatDetails(vatDetails: EncryptedVatDetails): VatDetails = {
    def d(field: String): String = encryptionService.decryptField(field)
    import vatDetails._

    val partOfVatGroupValue = d(partOfVatGroup) match {
      case null | "" | "null" => false  // Defaulting to `false` if it's null or empty
      case value =>
        // Attempt to convert the decrypted value to Boolean, handle potential exceptions
        Try(value.toBoolean).getOrElse {
          throw new IllegalArgumentException(s"Invalid Boolean value for partOfVatGroup: $value")
        }
    }

    VatDetails(
      registrationDate = registrationDate,
      address = decryptAddress(address),
      partOfVatGroup = partOfVatGroupValue,
      source = source
    )
  }

  def encryptRegistration(registration: Registration, vrn: Vrn): EncryptedRegistration = {
    def e(field: String): String = encryptionService.encryptField(field)

    EncryptedRegistration(
      vrn                   = vrn,
      registeredCompanyName = e(registration.registeredCompanyName),
      tradingNames          = registration.tradingNames.map(e),
      vatDetails            = encryptVatDetails(registration.vatDetails),
      euRegistrations       = registration.euRegistrations.map(encryptEuTaxRegistration),
      contactDetails        = encryptContactDetails(registration.contactDetails),
      websites              = registration.websites.map(e),
      commencementDate      = registration.commencementDate,
      previousRegistrations = registration.previousRegistrations.map(encryptPreviousRegistration),
      bankDetails           = encryptBankDetails(registration.bankDetails),
      isOnlineMarketplace   = e(registration.isOnlineMarketplace.toString),
      niPresence            = registration.niPresence,
      dateOfFirstSale       = registration.dateOfFirstSale,
      submissionReceived    = registration.submissionReceived,
      lastUpdated           = registration.lastUpdated,
      nonCompliantReturns   = registration.nonCompliantReturns,
      nonCompliantPayments  = registration.nonCompliantPayments
    )
  }

  def decryptRegistration(registration: EncryptedRegistration, vrn: Vrn): Registration = {
    def d(field: String): String = encryptionService.decryptField(field)

    Registration(
      vrn                   = vrn,
      registeredCompanyName = d(registration.registeredCompanyName),
      tradingNames          = registration.tradingNames.map(d),
      vatDetails            = decryptVatDetails(registration.vatDetails),
      euRegistrations       = registration.euRegistrations.map(decryptEuTaxRegistration),
      contactDetails        = decryptContactDetails(registration.contactDetails),
      websites              = registration.websites.map(d),
      commencementDate      = registration.commencementDate,
      previousRegistrations = registration.previousRegistrations.map(decryptPreviousRegistration),
      bankDetails           = decryptBankDetails(registration.bankDetails),
      isOnlineMarketplace   = d(registration.isOnlineMarketplace).toBoolean,
      niPresence            = registration.niPresence,
      dateOfFirstSale       = registration.dateOfFirstSale,
      submissionReceived    = registration.submissionReceived,
      lastUpdated           = registration.lastUpdated,
      nonCompliantReturns   = registration.nonCompliantReturns,
      nonCompliantPayments  = registration.nonCompliantPayments,
      adminUse              = AdminUse(registration.lastUpdated.map(x => LocalDateTime.ofInstant(x, clock.getZone)))
    )
  }
}
