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

package crypto

import models._
import models.EuTaxIdentifierType._
import uk.gov.hmrc.domain.Vrn

import javax.inject.Inject

class RegistrationEncrypter @Inject()(crypto: SecureGCMCipher) {

  def encryptCountry(country: Country, vrn: Vrn, key: String): EncryptedCountry = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, vrn.vrn, key)

    EncryptedCountry(e(country.code), e(country.name))
  }

  def decryptCountry(country: EncryptedCountry, vrn: Vrn, key: String): Country = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, vrn.vrn, key)
    import country._

    Country(d(code), d(name))
  }

  private def encryptUkAddress(address: UkAddress, vrn: Vrn, key: String): EncryptedUkAddress = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, vrn.vrn, key)
    import address._

    EncryptedUkAddress(e(line1), line2 map e, e(townOrCity), county map e, e(postCode))
  }

  private def decryptUkAddress(address: EncryptedUkAddress, vrn: Vrn, key: String): UkAddress = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, vrn.vrn, key)
    import address._

    UkAddress(d(line1), line2 map d, d(townOrCity), county map d, d(postCode))
  }

  private def encryptInternationalAddress(address: InternationalAddress, vrn: Vrn, key: String): EncryptedInternationalAddress = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, vrn.vrn, key)
    import address._

    EncryptedInternationalAddress(e(line1), line2 map e, e(townOrCity), stateOrRegion map e, postCode map e, country)
  }

  private def decryptInternationalAddress(address: EncryptedInternationalAddress, vrn: Vrn, key: String): InternationalAddress = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, vrn.vrn, key)
    import address._

    InternationalAddress(d(line1), line2 map d, d(townOrCity), stateOrRegion map d, postCode map d, country)
  }

  private def encryptDesAddress(address: DesAddress, vrn: Vrn, key: String): EncryptedDesAddress = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, vrn.vrn, key)
    import address._

    EncryptedDesAddress(e(line1), line2 map e, line3 map e, line4 map e, line5 map e, postCode map e, e(countryCode))
  }

  private def decryptDesAddress(address: EncryptedDesAddress, vrn: Vrn, key: String): DesAddress = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, vrn.vrn, key)
    import address._

    DesAddress(d(line1), line2 map d, line3 map d, line4 map d, line5 map d, postCode map d, d(countryCode))
  }

  def encryptAddress(address: Address, vrn: Vrn, key: String): EncryptedAddress = address match {
    case u: UkAddress            => encryptUkAddress(u, vrn, key)
    case i: InternationalAddress => encryptInternationalAddress(i, vrn, key)
    case d: DesAddress           => encryptDesAddress(d, vrn, key)
  }

  def decryptAddress(address: EncryptedAddress, vrn: Vrn, key: String): Address = address match {
    case u: EncryptedUkAddress            => decryptUkAddress(u, vrn, key)
    case i: EncryptedInternationalAddress => decryptInternationalAddress(i, vrn, key)
    case d: EncryptedDesAddress           => decryptDesAddress(d, vrn, key)
  }

  def encryptBankDetails(bankDetails: BankDetails, vrn: Vrn, key: String): EncryptedBankDetails = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, vrn.vrn, key)
    import bankDetails._

    EncryptedBankDetails(e(accountName), bic.map(b => e(b.toString)), e(iban.toString))
  }

  def decryptBankDetails(bankDetails: EncryptedBankDetails, vrn: Vrn, key: String): BankDetails = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, vrn.vrn, key)
    import bankDetails._

    val b = bic.map(x => Bic(d(x)).getOrElse(throw new EncryptionDecryptionException("decryptBankDetails", "Unable to decrypt BIC", "")))
    val i = Iban(d(iban)).right.getOrElse(throw new EncryptionDecryptionException("decryptBankDetails", "Unable to decrypt IBAN", ""))

    BankDetails(d(accountName), b, i)
  }

  def encryptContactDetails(contactDetails: ContactDetails, vrn: Vrn, key: String): EncryptedContactDetails = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, vrn.vrn, key)
    import contactDetails._

    EncryptedContactDetails(e(fullName), e(telephoneNumber), e(emailAddress))
  }

  def decryptContactDetails(contactDetails: EncryptedContactDetails, vrn: Vrn, key: String): ContactDetails = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, vrn.vrn, key)
    import contactDetails._

    ContactDetails(d(fullName), d(telephoneNumber), d(emailAddress))
  }

  def encryptEuTaxIdentifier(identifier: EuTaxIdentifier, vrn: Vrn, key: String): EncryptedEuTaxIdentifier = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, vrn.vrn, key)
    import identifier._

    EncryptedEuTaxIdentifier(e(identifierType.toString), e(value))
  }

  def decryptEuTaxIdentifier(identifier: EncryptedEuTaxIdentifier, vrn: Vrn, key: String): EuTaxIdentifier = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, vrn.vrn, key)

    def decryptIdentifierType(field: EncryptedValue): EuTaxIdentifierType =
      EuTaxIdentifierType.withName(d(field)).getOrElse(throw new Exception("Unable to decrypt value"))

    import identifier._

    EuTaxIdentifier(decryptIdentifierType(identifierType), d(value))
  }

  private def encryptEuVatRegistration(registration: EuVatRegistration, vrn: Vrn, key: String): EncryptedEuVatRegistration = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, vrn.vrn, key)
    import registration._

    EncryptedEuVatRegistration(encryptCountry(country, vrn, key), e(vatNumber))
  }

  private def decryptEuVatRegistration(registration: EncryptedEuVatRegistration, vrn: Vrn, key: String): EuVatRegistration = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, vrn.vrn, key)
    import registration._

    EuVatRegistration(decryptCountry(country, vrn, key), d(vatNumber))
  }

  private def encryptRegistrationWithoutFixedEstablishment(registration: RegistrationWithoutFixedEstablishment, vrn: Vrn, key: String): EncryptedRegistrationWithoutFixedEstablishment = {
    import registration._

    EncryptedRegistrationWithoutFixedEstablishment(
      encryptCountry(country, vrn, key),
      encryptEuTaxIdentifier(taxIdentifier, vrn, key),
      sendsGoods.map(s => crypto.encrypt(s.toString, vrn.vrn, key))
    )
  }

  private def decryptRegistrationWithoutFixedEstablishment(registration: EncryptedRegistrationWithoutFixedEstablishment, vrn: Vrn, key: String): RegistrationWithoutFixedEstablishment = {
    import registration._

    RegistrationWithoutFixedEstablishment(
      decryptCountry(country, vrn, key),
      decryptEuTaxIdentifier(taxIdentifier, vrn, key),
      sendsGoods.map(crypto.decrypt(_, vrn.vrn, key).toBoolean)
    )
  }


  private def encryptRegistrationSendingGoods(registration: RegistrationSendingGoods, vrn: Vrn, key: String): EncryptedRegistrationSendingGoods = {
    import registration._

    EncryptedRegistrationSendingGoods(
      encryptCountry(country, vrn, key),
      encryptEuTaxIdentifier(taxIdentifier, vrn, key),
      crypto.encrypt(sendsGoods.toString, vrn.vrn, key),
      crypto.encrypt(tradingName, vrn.vrn, key),
      encryptInternationalAddress(address, vrn, key)
    )
  }

  private def decryptRegistrationSendingGoods(registration: EncryptedRegistrationSendingGoods, vrn: Vrn, key: String): RegistrationSendingGoods = {
    import registration._

    RegistrationSendingGoods(
      decryptCountry(country, vrn, key),
      decryptEuTaxIdentifier(taxIdentifier, vrn, key),
      crypto.decrypt(sendsGoods, vrn.vrn, key).toBoolean,
      crypto.decrypt(tradingName, vrn.vrn, key),
      decryptInternationalAddress(address, vrn, key)
    )
  }

  private def encryptRegistrationWithFixedEstablishment(
                                                         registration: RegistrationWithFixedEstablishment,
                                                         vrn: Vrn,
                                                         key: String
                                                       ): EncryptedRegistrationWithFixedEstablishment = {
    import registration._

    EncryptedRegistrationWithFixedEstablishment(
      encryptCountry(country, vrn, key),
      encryptEuTaxIdentifier(taxIdentifier, vrn, key),
      encryptFixedEstablishment(fixedEstablishment, vrn, key)
    )
  }

  private def decryptRegistrationWithFixedEstablishment(
                                                         registration: EncryptedRegistrationWithFixedEstablishment,
                                                         vrn: Vrn,
                                                         key: String
                                                       ): RegistrationWithFixedEstablishment = {
    import registration._

    RegistrationWithFixedEstablishment(
      decryptCountry(country, vrn, key),
      decryptEuTaxIdentifier(taxIdentifier, vrn, key),
      decryptFixedEstablishment(fixedEstablishment, vrn, key)
    )
  }

  private def encryptRegistrationWithoutFixedEstablishment(
                                                            registration: RegistrationWithoutTaxId,
                                                            vrn: Vrn,
                                                            key: String
                                                          ) : EncryptedRegistrationWithoutTaxId =
    EncryptedRegistrationWithoutTaxId(encryptCountry(registration.country, vrn, key))

  private def decryptRegistrationWithoutFixedEstablishment(
                                                            registration: EncryptedRegistrationWithoutTaxId,
                                                            vrn: Vrn,
                                                            key: String
                                                          ) : RegistrationWithoutTaxId =
    RegistrationWithoutTaxId(decryptCountry(registration.country, vrn, key))

  def encryptEuTaxRegistration(registration: EuTaxRegistration, vrn: Vrn, key: String): EncryptedEuTaxRegistration =
    registration match {
      case v: EuVatRegistration                     => encryptEuVatRegistration(v, vrn, key)
      case wf: RegistrationSendingGoods             => encryptRegistrationSendingGoods(wf, vrn, key)
      case wf: RegistrationWithoutFixedEstablishment   => encryptRegistrationWithoutFixedEstablishment(wf, vrn, key)
      case f: RegistrationWithFixedEstablishment    => encryptRegistrationWithFixedEstablishment(f, vrn, key)
      case w: RegistrationWithoutTaxId => encryptRegistrationWithoutFixedEstablishment(w, vrn, key)
    }

  def decryptEuTaxRegistration(registration: EncryptedEuTaxRegistration, vrn: Vrn, key: String): EuTaxRegistration =
    registration match {
      case v: EncryptedEuVatRegistration                    => decryptEuVatRegistration(v, vrn, key)
      case wf: EncryptedRegistrationSendingGoods              => decryptRegistrationSendingGoods(wf, vrn, key)
      case wf: EncryptedRegistrationWithoutFixedEstablishment   => decryptRegistrationWithoutFixedEstablishment(wf, vrn, key)
      case f: EncryptedRegistrationWithFixedEstablishment    => decryptRegistrationWithFixedEstablishment(f, vrn, key)
      case w: EncryptedRegistrationWithoutTaxId => decryptRegistrationWithoutFixedEstablishment(w, vrn, key)
    }

  private def encryptFixedEstablishment(fixedEstablishment: FixedEstablishment, vrn: Vrn, key: String): EncryptedFixedEstablishment = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, vrn.vrn, key)
    import fixedEstablishment._

    EncryptedFixedEstablishment(e(tradingName), encryptInternationalAddress(address, vrn, key))
  }

  private def decryptFixedEstablishment(fixedEstablishment: EncryptedFixedEstablishment, vrn: Vrn, key: String): FixedEstablishment = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, vrn.vrn, key)
    import fixedEstablishment._

    FixedEstablishment(d(tradingName), decryptInternationalAddress(address, vrn, key))
  }

  def encryptedPreviousRegistration(registration: PreviousRegistration, vrn: Vrn, key: String): EncryptedPreviousRegistration = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, vrn.vrn, key)
    import registration._

    EncryptedPreviousRegistration(encryptCountry(country, vrn, key), e(vatNumber))
  }

  def decryptedPreviousRegistration(registration: EncryptedPreviousRegistration, vrn: Vrn, key: String): PreviousRegistration = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, vrn.vrn, key)
    import registration._

    PreviousRegistration(decryptCountry(country, vrn, key), d(vatNumber))
  }

  def encryptVatDetails(vatDetails: VatDetails, vrn: Vrn, key: String): EncryptedVatDetails = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, vrn.vrn, key)
    import vatDetails._

    EncryptedVatDetails(registrationDate, encryptAddress(address, vrn, key), e(partOfVatGroup.toString), source)
  }

  def decryptVatDetails(vatDetails: EncryptedVatDetails, vrn: Vrn, key: String): VatDetails = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, vrn.vrn, key)
    import vatDetails._

    VatDetails(registrationDate, decryptAddress(address, vrn, key), d(partOfVatGroup).toBoolean, source)
  }

  def encryptRegistration(registration: Registration, vrn: Vrn, key: String): EncryptedRegistration = {
    def e(field: String): EncryptedValue = crypto.encrypt(field, vrn.vrn, key)

    EncryptedRegistration(
      vrn                   = vrn,
      registeredCompanyName = e(registration.registeredCompanyName),
      tradingNames          = registration.tradingNames.map(e),
      vatDetails            = encryptVatDetails(registration.vatDetails, vrn, key),
      euRegistrations       = registration.euRegistrations.map(encryptEuTaxRegistration(_, vrn, key)),
      contactDetails        = encryptContactDetails(registration.contactDetails, vrn, key),
      websites              = registration.websites.map(e),
      commencementDate      = registration.commencementDate,
      previousRegistrations = registration.previousRegistrations.map(encryptedPreviousRegistration(_, vrn, key)),
      bankDetails           = encryptBankDetails(registration.bankDetails, vrn, key),
      isOnlineMarketplace   = e(registration.isOnlineMarketplace.toString),
      niPresence            = registration.niPresence,
      dateOfFirstSale       = registration.dateOfFirstSale,
      submissionReceived    = registration.submissionReceived,
      lastUpdated           = registration.lastUpdated,
      nonCompliantReturns   = registration.nonCompliantReturns,
      nonCompliantPayments  = registration.nonCompliantPayments
    )
  }

  def decryptRegistration(registration: EncryptedRegistration, vrn: Vrn, key: String): Registration = {
    def d(field: EncryptedValue): String = crypto.decrypt(field, vrn.vrn, key)

    Registration(
      vrn                   = vrn,
      registeredCompanyName = d(registration.registeredCompanyName),
      tradingNames          = registration.tradingNames.map(d),
      vatDetails            = decryptVatDetails(registration.vatDetails, vrn, key),
      euRegistrations       = registration.euRegistrations.map(decryptEuTaxRegistration(_, vrn, key)),
      contactDetails        = decryptContactDetails(registration.contactDetails, vrn, key),
      websites              = registration.websites.map(d),
      commencementDate      = registration.commencementDate,
      previousRegistrations = registration.previousRegistrations.map(decryptedPreviousRegistration(_, vrn, key)),
      bankDetails           = decryptBankDetails(registration.bankDetails, vrn, key),
      isOnlineMarketplace   = d(registration.isOnlineMarketplace).toBoolean,
      niPresence            = registration.niPresence,
      dateOfFirstSale       = registration.dateOfFirstSale,
      submissionReceived    = registration.submissionReceived,
      lastUpdated           = registration.lastUpdated,
      nonCompliantReturns   = registration.nonCompliantReturns,
      nonCompliantPayments  = registration.nonCompliantPayments
    )
  }
}
