package crypto

import base.BaseSpec
import com.typesafe.config.Config
import config.AppConfig
import generators.Generators
import models._
import models.etmp.AdminUse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Configuration
import services.crypto.EncryptionService

import java.time.{Instant, LocalDate, LocalDateTime}

class RegistrationEncrypterSpec extends BaseSpec with ScalaCheckPropertyChecks with Generators with MockitoSugar {

  private val appConfig = mock[AppConfig]
  private val cipher    = new AesGCMCrypto
  private val mockConfiguration = mock[Configuration]
  private val mockConfig = mock[Config]
  private val mockEncryptionService: EncryptionService = new EncryptionService(mockConfiguration)
  private val encrypter = new RegistrationEncrypter(appConfig, cipher, mockEncryptionService, stubClock)
  private val secretKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="

  when(mockConfiguration.underlying) thenReturn mockConfig
  when(mockConfig.getString(any())) thenReturn secretKey

  "encrypt / decrypt country" - {

    "must encrypt a country and decrypt it" in {
      forAll(arbitrary[Country]) {
        country =>
          val e = encrypter.encryptCountry(country)
          val d = encrypter.decryptCountry(e)

          d mustEqual country
      }
    }
  }

  "encrypt / decrypt address" - {

    "must encrypt a UK address and decrypt it" in {
      forAll(arbitrary[UkAddress]) {
        address =>
          val e = encrypter.encryptAddress(address)
          val d = encrypter.decryptAddress(e)

          d mustEqual address
      }
    }

    "must encrypt an international address and decrypt it" in {
      forAll(arbitrary[InternationalAddress]) {
        address =>
          val e = encrypter.encryptAddress(address)
          val d = encrypter.decryptAddress(e)

          d mustEqual address
      }
    }
  }

  "encrypt / decrypt bank details" - {

    "must encrypt a set of bank details and decrypt them" in {
      forAll(arbitrary[BankDetails]) {
        bankDetails =>
          val e = encrypter.encryptBankDetails(bankDetails)
          val d = encrypter.decryptBankDetails(e)

          d mustEqual bankDetails
      }
    }

    "must throw exception if IBAN cannot be decrypted" in {
      forAll(arbitrary[BankDetails]) {
        bankDetails =>
          val e = encrypter.encryptBankDetails(bankDetails.copy(iban = Iban("ZZ", "zz", "zz")))
          val d = intercept[EncryptionDecryptionException](
            encrypter.decryptBankDetails(e)
          )
          d.failureReason must include("Unable to decrypt IBAN")

      }
    }

    "must throw exception if BIC cannot be decrypted" in {
      forAll(arbitrary[BankDetails]) {
        bankDetails =>
          val e = encrypter.encryptBankDetails(bankDetails)
          val z = e.copy(bic = Some(e.iban))

          val d = intercept[EncryptionDecryptionException](
            encrypter.decryptBankDetails(z)
          )
          d.failureReason must include("Unable to decrypt BIC")

      }
    }
  }

  "encrypt / decrypt contact details" - {

    "must encrypt a set of contact details and decrypt them" in {
      forAll(arbitrary[ContactDetails]) {
        contactDetails =>
          val e = encrypter.encryptContactDetails(contactDetails)
          val d = encrypter.decryptContactDetails(e)

          d mustEqual contactDetails
      }
    }
  }

  "encrypt / decrypt EU Tax identifier" - {

    "must encrypt a tax identifier and decrypt it" in {
      forAll(arbitrary[EuTaxIdentifier]) {
        taxIdentifier =>
          val e = encrypter.encryptEuTaxIdentifier(taxIdentifier)
          val d = encrypter.decryptEuTaxIdentifier(e)

          d mustEqual taxIdentifier
      }
    }

    "must throw exception if tax identifier cannot be decrypted" in {
      forAll(arbitrary[EuTaxIdentifier]) {
        taxIdentifier =>
          val e = encrypter.encryptEuTaxIdentifier(taxIdentifier)
          val z = e.copy(identifierType = e.value)
          val d = intercept[Exception](
            encrypter.decryptEuTaxIdentifier(z)
          )
          d.getMessage must include("Unable to decrypt value")
      }
    }
  }

  "encrypt / decrypt EU Tax Registration" - {

    "must encrypt an EU VAT Registration and decrypt it" in {
      forAll(arbitrary[EuVatRegistration]) {
        registration =>
          val e = encrypter.encryptEuTaxRegistration(registration)
          val d = encrypter.decryptEuTaxRegistration(e)

          d mustEqual registration
      }
    }

    "must encrypt a registration with fixed establishment and decrypt it" in {
      forAll(arbitrary[RegistrationWithFixedEstablishment]) {
        registration =>
          val e = encrypter.encryptEuTaxRegistration(registration)
          val d = encrypter.decryptEuTaxRegistration(e)

          d mustEqual registration
      }
    }

    "must encrypt a registration without fixed establishment with trade details and decrypt it" in {
      forAll(arbitrary[RegistrationWithoutFixedEstablishmentWithTradeDetails]) {
        registration =>
          val e = encrypter.encryptEuTaxRegistration(registration)
          val d = encrypter.decryptEuTaxRegistration(e)

          d mustEqual registration
      }
    }

    "must encrypt a registration without a taxId and decrypt it" in {
      forAll(arbitrary[RegistrationWithoutTaxId]) {
        registration =>
          val e = encrypter.encryptEuTaxRegistration(registration)
          val d = encrypter.decryptEuTaxRegistration(e)

          d mustEqual registration
      }
    }
  }

  "encrypt / decrypt previous registration" - {

    "must encrypt a previous registration and decrypt it" in {

      forAll(arbitrary[PreviousRegistration]) {
        registration =>
          val e = encrypter.encryptPreviousRegistration(registration)
          val d = encrypter.decryptPreviousRegistration(e)

          d mustEqual registration
      }
    }
  }

  "encrypt / decrypt VAT details" - {

    "must encrypt a set of VAT details and decrypt it" in {
      forAll(arbitrary[VatDetails]) {
        vatDetails =>
          val e = encrypter.encryptVatDetails(vatDetails)
          val d = encrypter.decryptVatDetails(e)

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
        dateOfFirstSale       = None,
        submissionReceived    = Some(Instant.now(stubClock)),
        lastUpdated           = Some(Instant.now(stubClock)),
        nonCompliantReturns   = Some("1"),
        nonCompliantPayments  = Some("2"),
        adminUse              = AdminUse(Some(LocalDateTime.now(stubClock)))
      )

      val e = encrypter.encryptRegistration(registration, vrn)
      val d = encrypter.decryptRegistration(e, vrn)

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
        dateOfFirstSale       = Some(LocalDate.now),
        submissionReceived    = Some(Instant.now(stubClock)),
        lastUpdated           = Some(Instant.now(stubClock)),
        nonCompliantReturns   = Some("1"),
        nonCompliantPayments  = Some("2"),
        adminUse              = AdminUse(Some(LocalDateTime.now(stubClock)))
      )

      val e = encrypter.encryptRegistration(registration, vrn)
      val d = encrypter.decryptRegistration(e, vrn)

      d mustEqual registration
    }
  }
}
