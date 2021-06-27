package crypto

import base.BaseSpec
import generators.Generators
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.{Instant, LocalDate}

class RegistrationEncrypterSpec extends BaseSpec with ScalaCheckPropertyChecks with Generators {

  private val cipher    = new SecureGCMCipher
  private val encrypter = new RegistrationEncrypter(cipher)
  private val secretKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="

  "encrypt / decrypt country" - {

    "must encrypt a country and decrypt it" in {
      forAll(arbitrary[Country]) {
        country =>
          val e = encrypter.encryptCountry(country, vrn, secretKey)
          val d = encrypter.decryptCountry(e, vrn, secretKey)

          d mustEqual country
      }
    }
  }

  "encrypt / decrypt address" - {

    "must encrypt a UK address and decrypt it" in {
      forAll(arbitrary[UkAddress]) {
        address =>
          val e = encrypter.encryptAddress(address, vrn, secretKey)
          val d = encrypter.decryptAddress(e, vrn, secretKey)

          d mustEqual address
      }
    }

    "must encrypt an international address and decrypt it" in {
      forAll(arbitrary[InternationalAddress]) {
        address =>
          val e = encrypter.encryptAddress(address, vrn, secretKey)
          val d = encrypter.decryptAddress(e, vrn, secretKey)

          d mustEqual address
      }
    }
  }

  "encrypt / decrypt bank details" - {

    "must encrypt a set of bank details and decrypt them" in {
      forAll(arbitrary[BankDetails]) {
        bankDetails =>
          val e = encrypter.encryptBankDetails(bankDetails, vrn, secretKey)
          val d = encrypter.decryptBankDetails(e, vrn, secretKey)

          d mustEqual bankDetails
      }
    }
  }

  "encrypt / decrypt contact details" - {

    "must encrypt a set of contact details and decrypt them" in {
      forAll(arbitrary[ContactDetails]) {
        contactDetails =>
          val e = encrypter.encryptContactDetails(contactDetails, vrn, secretKey)
          val d = encrypter.decryptContactDetails(e, vrn, secretKey)

          d mustEqual contactDetails
      }
    }
  }

  "encrypt / decrypt EU Tax identifier" - {

    "must encrypt a tax identifier and decrypt it" in {
      forAll(arbitrary[EuTaxIdentifier]) {
        taxIdentifier =>
          val e = encrypter.encryptEuTaxIdentifier(taxIdentifier, vrn, secretKey)
          val d = encrypter.decryptEuTaxIdentifier(e, vrn, secretKey)

          d mustEqual taxIdentifier
      }
    }
  }

  "encrypt / decrypt EU Tax Registration" - {

    "must encrypt an EU VAT Registration and decrypt it" in {
      forAll(arbitrary[EuVatRegistration]) {
        registration =>
          val e = encrypter.encryptEuTaxRegistration(registration, vrn, secretKey)
          val d = encrypter.decryptEuTaxRegistration(e, vrn, secretKey)

          d mustEqual registration
      }
    }

    "must encrypt a registration with fixed establishment and decrypt it" in {
      forAll(arbitrary[RegistrationWithFixedEstablishment]) {
        registration =>
          val e = encrypter.encryptEuTaxRegistration(registration, vrn, secretKey)
          val d = encrypter.decryptEuTaxRegistration(e, vrn, secretKey)

          d mustEqual registration
      }
    }

    "must encrypt a registration without a fixed establishment and decrypt it" in {
      forAll(arbitrary[RegistrationWithoutFixedEstablishment]) {
        registration =>
          val e = encrypter.encryptEuTaxRegistration(registration, vrn, secretKey)
          val d = encrypter.decryptEuTaxRegistration(e, vrn, secretKey)

          d mustEqual registration
      }
    }
  }

  "encrypt / decrypt previous registration" - {

    "must encrypt a previous registration and decrypt it" in {

      forAll(arbitrary[PreviousRegistration]) {
        registration =>
          val e = encrypter.encryptedPreviousRegistration(registration, vrn, secretKey)
          val d = encrypter.decryptedPreviousRegistration(e, vrn, secretKey)

          d mustEqual registration
      }
    }
  }

  "encrypt / decrypt VAT details" - {

    "must encrypt a set of VAT details and decrypt it" in {
      forAll(arbitrary[VatDetails]) {
        vatDetails =>
          val e = encrypter.encryptVatDetails(vatDetails, vrn, secretKey)
          val d = encrypter.decryptVatDetails(e, vrn, secretKey)

          d mustEqual vatDetails
      }
    }
  }

  "encrypt / decrypt registration" - {

    "must encrypt a registration with all options missing and decrypt it" in {

      val registration = Registration(
        vrn                   = vrn,
        registeredCompanyName = arbitrary[String].sample.value,
        tradingNames          = Seq.empty,
        vatDetails            = arbitrary[VatDetails].sample.value,
        euRegistrations       = Seq.empty,
        contactDetails        = arbitrary[ContactDetails].sample.value,
        websites              = Seq.empty,
        commencementDate      = LocalDate.now,
        previousRegistrations = Seq.empty,
        bankDetails           = arbitrary[BankDetails].sample.value,
        isOnlineMarketplace   = arbitrary[Boolean].sample.value,
        niPresence            = None,
        submissionReceived    = Instant.now(stubClock)
      )

      val e = encrypter.encryptRegistration(registration, vrn, secretKey)
      val d = encrypter.decryptRegistration(e, vrn, secretKey)

      d mustEqual registration
    }

    "must encrypt a registration with all options present and decrypt it" in {

      val registration = Registration(
        vrn                   = vrn,
        registeredCompanyName = arbitrary[String].sample.value,
        tradingNames          = Gen.listOfN(10, arbitrary[String]).sample.value,
        vatDetails            = arbitrary[VatDetails].sample.value,
        euRegistrations       = Gen.listOfN(10, arbitrary[EuTaxRegistration]).sample.value,
        contactDetails        = arbitrary[ContactDetails].sample.value,
        websites              = Gen.listOfN(10, arbitrary[String]).sample.value,
        commencementDate      = LocalDate.now,
        previousRegistrations = Gen.listOfN(10, arbitrary[PreviousRegistration]).sample.value,
        bankDetails           = arbitrary[BankDetails].sample.value,
        isOnlineMarketplace   = arbitrary[Boolean].sample.value,
        niPresence            = Some(arbitrary[NiPresence].sample.value),
        submissionReceived    = Instant.now(stubClock)
      )

      val e = encrypter.encryptRegistration(registration, vrn, secretKey)
      val d = encrypter.decryptRegistration(e, vrn, secretKey)

      d mustEqual registration
    }
  }
}
