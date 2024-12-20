package generators

import crypto.EncryptedValue
import models.{enrolments, *}
import models.enrolments.EnrolmentStatus
import models.etmp.*
import models.exclusions.{ExcludedTrader, ExclusionReason}
import models.requests.SaveForLaterRequest
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.domain.Vrn

import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}

trait Generators {

  implicit lazy val arbitrarySalesChannels: Arbitrary[SalesChannels] =
    Arbitrary {
      Gen.oneOf(SalesChannels.values)
    }

  implicit lazy val arbitraryNiPresence: Arbitrary[NiPresence] =
    Arbitrary {
      Gen.oneOf(
        Gen.const(PrincipalPlaceOfBusinessInNi),
        Gen.const(FixedEstablishmentInNi),
        arbitrary[SalesChannels].map(NoPresence(_))
      )
    }

  implicit lazy val arbitraryBic: Arbitrary[Bic] = {
    val asciiCodeForA = 65
    val asciiCodeForN = 78
    val asciiCodeForP = 80
    val asciiCodeForZ = 90

    Arbitrary {
      for {
        firstChars <- Gen.listOfN(6, Gen.alphaUpperChar).map(_.mkString)
        char7 <- Gen.oneOf(Gen.alphaUpperChar, Gen.choose(2, 9).map(_.toString.head))
        char8 <- Gen.oneOf(
          Gen.choose(asciiCodeForA, asciiCodeForN).map(_.toChar),
          Gen.choose(asciiCodeForP, asciiCodeForZ).map(_.toChar),
          Gen.choose(0, 9).map(_.toString.head)
        )
        lastChars <- Gen.option(Gen.listOfN(3, Gen.oneOf(Gen.alphaUpperChar, Gen.numChar)).map(_.mkString))
      } yield Bic(s"$firstChars$char7$char8${lastChars.getOrElse("")}").get
    }
  }

  implicit lazy val arbitraryIban: Arbitrary[Iban] =
    Arbitrary {
      Gen.oneOf(
        "GB94BARC10201530093459",
        "GB33BUKB20201555555555",
        "DE29100100100987654321",
        "GB24BKEN10000031510604",
        "GB27BOFI90212729823529",
        "GB17BOFS80055100813796",
        "GB92BARC20005275849855",
        "GB66CITI18500812098709",
        "GB15CLYD82663220400952",
        "GB26MIDL40051512345674",
        "GB76LOYD30949301273801",
        "GB25NWBK60080600724890",
        "GB60NAIA07011610909132",
        "GB29RBOS83040210126939",
        "GB79ABBY09012603367219",
        "GB21SCBL60910417068859",
        "GB42CPBK08005470328725"
      ).map(v => Iban(v).toOption.get)
    }

  implicit lazy val arbitraryEncryptedCountry: Arbitrary[EncryptedCountry] =
    Arbitrary {
      for {
        code <- arbitrary[EncryptedValue]
        name <- arbitrary[EncryptedValue]
      } yield EncryptedCountry(code.value, name.value)
    }

  implicit lazy val arbitraryEncryptedValue: Arbitrary[EncryptedValue] =
    Arbitrary {
      for {
        value <- Gen.listOfN(50, Gen.alphaNumChar).map(_.mkString)
        nonce <- Gen.listOfN(50, Gen.alphaNumChar).map(_.mkString)
      } yield EncryptedValue(value, nonce)
    }

  implicit lazy val arbitraryVatDetails: Arbitrary[VatDetails] =
    Arbitrary {
      for {
        registrationDate <- arbitrary[Int].map(n => LocalDate.ofEpochDay(n))
        address <- arbitrary[Address]
        partOfVatGroup <- arbitrary[Boolean]
        source <- arbitrary[VatDetailSource]
      } yield VatDetails(registrationDate, address, partOfVatGroup, source)
    }

  implicit val arbitraryVatDetailSource: Arbitrary[VatDetailSource] =
    Arbitrary(
      Gen.oneOf(VatDetailSource.values)
    )

  implicit lazy val arbitraryPreviousSchemeDetails: Arbitrary[PreviousSchemeDetails] =
    Arbitrary {
      for {
        previousScheme <- Gen.oneOf(PreviousScheme.values)
        previousSchemeNumber <- Gen.listOfN(11, Gen.alphaChar).map(_.mkString)
      } yield PreviousSchemeDetails(previousScheme, PreviousSchemeNumbers(previousSchemeNumber, None))
    }

  implicit lazy val arbitraryPreviousRegistration: Arbitrary[PreviousRegistration] =
    Arbitrary {
      Gen.oneOf(arbitrary[PreviousRegistrationNew], arbitrary[PreviousRegistrationLegacy])
    }

  implicit lazy val arbitraryPreviousRegistrationNew: Arbitrary[PreviousRegistrationNew] =
    Arbitrary {
      for {
        country <- arbitrary[Country]
        previousSchemeDetails <- Gen.listOfN(2, arbitrary[PreviousSchemeDetails])
      } yield PreviousRegistrationNew(country, previousSchemeDetails)
    }

  implicit lazy val arbitraryPreviousRegistrationLegacy: Arbitrary[PreviousRegistrationLegacy] =
    Arbitrary {
      for {
        country <- arbitrary[Country]
        vatNumber <- arbitrary[String]
      } yield PreviousRegistrationLegacy(country, vatNumber)
    }

  implicit lazy val arbitraryEuTaxRegistration: Arbitrary[EuTaxRegistration] =
    Arbitrary {
      Gen.oneOf(arbitrary[RegistrationWithFixedEstablishment].sample)
    }

  implicit lazy val arbitraryEuVatRegistration: Arbitrary[EuVatRegistration] =
    Arbitrary {
      for {
        country <- arbitrary[Country]
        vatNumber <- arbitrary[String]
      } yield EuVatRegistration(country, vatNumber)
    }

  implicit lazy val arbitraryRegistrationWithoutFixedEstablishmentWithTradeDetails: Arbitrary[RegistrationWithoutFixedEstablishmentWithTradeDetails] =
    Arbitrary {
      for {
        country <- arbitrary[Country]
        taxIdentifier <- arbitrary[EuTaxIdentifier]
        tradingName <- arbitrary[String]
        address <- arbitraryInternationalAddress.arbitrary
      } yield RegistrationWithoutFixedEstablishmentWithTradeDetails(country,
        taxIdentifier,
        TradeDetails(
          tradingName,
          address)
      )
    }

  implicit lazy val arbitraryRegistrationWithFixedEstablishment: Arbitrary[RegistrationWithFixedEstablishment] =
    Arbitrary {
      for {
        country <- arbitrary[Country]
        taxIdentifier <- arbitrary[EuTaxIdentifier]
        fixedEstablishment <- arbitrary[TradeDetails]
      } yield RegistrationWithFixedEstablishment(country, taxIdentifier, fixedEstablishment)
    }

  implicit lazy val arbitraryRegistrationWithoutTaxId: Arbitrary[RegistrationWithoutTaxId] =
    Arbitrary {
      arbitrary[Country].map(c => RegistrationWithoutTaxId(c))
    }

  implicit lazy val arbitraryUkAddress: Arbitrary[UkAddress] =
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- Gen.option(arbitrary[String])
        townOrCity <- arbitrary[String]
        county <- Gen.option(arbitrary[String])
        postCode <- arbitrary[String]
      } yield UkAddress(line1, line2, townOrCity, county, postCode)
    }

  implicit val arbitraryAddress: Arbitrary[Address] =
    Arbitrary {
      Gen.oneOf(
        arbitrary[UkAddress],
        arbitrary[InternationalAddress],
        arbitrary[DesAddress]
      )
    }

  implicit lazy val arbitraryInternationalAddress: Arbitrary[InternationalAddress] =
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- Gen.option(arbitrary[String])
        townOrCity <- arbitrary[String]
        stateOrRegion <- Gen.option(arbitrary[String])
        postCode <- Gen.option(arbitrary[String])
        country <- arbitrary[Country]
      } yield InternationalAddress(line1, line2, townOrCity, stateOrRegion, postCode, country)
    }

  implicit lazy val arbitraryDesAddress: Arbitrary[DesAddress] =
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- Gen.option(arbitrary[String])
        line3 <- Gen.option(arbitrary[String])
        line4 <- Gen.option(arbitrary[String])
        line5 <- Gen.option(arbitrary[String])
        postCode <- Gen.option(arbitrary[String])
        countryCode <- Gen.listOfN(2, Gen.alphaChar).map(_.mkString)
      } yield DesAddress(line1, line2, line3, line4, line5, postCode, countryCode)
    }

  implicit lazy val arbitraryBankDetails: Arbitrary[BankDetails] =
    Arbitrary {
      for {
        accountName <- arbitrary[String]
        bic <- Gen.option(arbitrary[Bic])
        iban <- arbitrary[Iban]
      } yield BankDetails(accountName, bic, iban)
    }

  implicit lazy val arbitraryAdminUse: Arbitrary[AdminUse] =
    Arbitrary {
      for {
        changeDate <- arbitrary[LocalDateTime]
      } yield AdminUse(Some(changeDate))
    }

  implicit val arbitraryEuTaxIdentifierType: Arbitrary[EuTaxIdentifierType] =
    Arbitrary {
      Gen.oneOf(EuTaxIdentifierType.values)
    }

  implicit val arbitraryEuTaxIdentifier: Arbitrary[EuTaxIdentifier] =
    Arbitrary {
      for {
        identifierType <- arbitrary[EuTaxIdentifierType]
        value <- arbitrary[Int].map(_.toString)
      } yield EuTaxIdentifier(identifierType, value)
    }

  implicit lazy val arbitraryFixedEstablishment: Arbitrary[TradeDetails] =
    Arbitrary {
      for {
        tradingName <- arbitrary[String]
        address <- arbitrary[InternationalAddress]
      } yield TradeDetails(tradingName, address)
    }


  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      for {
        char1 <- Gen.alphaUpperChar
        char2 <- Gen.alphaUpperChar
        name <- arbitrary[String]
      } yield Country(s"$char1$char2", name)
    }

  implicit lazy val arbitraryBusinessContactDetails: Arbitrary[ContactDetails] =
    Arbitrary {
      for {
        fullName <- arbitrary[String]
        telephoneNumber <- arbitrary[String]
        emailAddress <- arbitrary[String]
      } yield ContactDetails(fullName, telephoneNumber, emailAddress)
    }

  implicit val arbitraryVrn: Arbitrary[Vrn] =
    Arbitrary {
      Gen.listOfN(9, Gen.numChar).map(_.mkString).map(Vrn)
    }

  implicit val arbitrarySavedUserAnswers: Arbitrary[SavedUserAnswers] =
    Arbitrary {
      for {
        vrn <- arbitrary[Vrn]
        data = JsObject(Seq(
          "test" -> Json.toJson("test")
        ))
        now = Instant.now
      } yield SavedUserAnswers(
        vrn, data, now)
    }

  implicit val arbitrarySaveForLaterRequest: Arbitrary[SaveForLaterRequest] = {
    Arbitrary {
      for {
        vrn <- arbitrary[Vrn]
        data = JsObject(Seq(
          "test" -> Json.toJson("test")
        ))
      } yield SaveForLaterRequest(vrn, data)
    }
  }

  implicit val arbitraryEtmpTradingNames: Arbitrary[EtmpTradingNames] = {
    Arbitrary {
      for {
        tradingName <- arbitrary[String]
      } yield EtmpTradingNames(tradingName)
    }
  }

  implicit val arbitraryEtmpEuRegistrationDetails: Arbitrary[EtmpEuRegistrationDetails] = {
    Arbitrary {
      for {
        countryOfRegistration <- Gen.listOfN(2, Gen.alphaChar).map(_.mkString.toUpperCase)
        vatNumber <- Gen.option(arbitraryVrn.arbitrary.toString)
        taxIdentificationNumber <- Gen.option(arbitrary[String])
        fixedEstablishment <- Gen.option(arbitrary[Boolean])
        tradingName <- Gen.option(arbitrary[String])
        fixedEstablishmentAddressLine1 <- Gen.option(arbitrary[String])
        fixedEstablishmentAddressLine2 <- Gen.option(arbitrary[String])
        townOrCity <- Gen.option(arbitrary[String])
        regionOrState <- Gen.option(arbitrary[String])
        postcode <- Gen.option(arbitrary[String])
      } yield
        EtmpEuRegistrationDetails(
          countryOfRegistration,
          vatNumber,
          taxIdentificationNumber,
          fixedEstablishment,
          tradingName,
          fixedEstablishmentAddressLine1,
          fixedEstablishmentAddressLine2,
          townOrCity,
          regionOrState,
          postcode
        )
    }
  }

  implicit val arbitraryEtmpPreviousEURegistrationDetails: Arbitrary[EtmpPreviousEURegistrationDetails] = {
    Arbitrary {
      for {
        issuedBy <- arbitrary[String]
        registrationNumber <- arbitrary[String]
        schemeType <- Gen.oneOf(SchemeType.values)
        intermediaryNumber <- Gen.option(arbitrary[String])
      } yield EtmpPreviousEURegistrationDetails(issuedBy, registrationNumber, schemeType, intermediaryNumber)
    }
  }

  implicit val arbitraryWebsite: Arbitrary[Website] = {
    Arbitrary {
      for {
        websiteAddress <- arbitrary[String]
      } yield Website(websiteAddress)
    }
  }

  implicit val arbitraryEtmpExclusion: Arbitrary[EtmpExclusion] = {

    Arbitrary {
      for {
        exclusionReason <- Gen.oneOf[ExclusionReason](ExclusionReason.values)
        effectiveDate <- arbitrary[Int].map(n => LocalDate.ofEpochDay(n))
        decisionDate <- arbitrary[Int].map(n => LocalDate.ofEpochDay(n))
        quarantine <- arbitrary[Boolean]
      } yield {
        EtmpExclusion(
          exclusionReason = exclusionReason,
          effectiveDate = effectiveDate,
          decisionDate = decisionDate,
          quarantine = quarantine
        )
      }
    }
  }

  implicit val arbitraryEtmpSchemeDetails: Arbitrary[EtmpSchemeDetails] = {
    Arbitrary {
      for {
        commencementDate <- arbitrary[String]
        firstSaleDate <- Gen.option(arbitrary[String])
        euRegistrationDetails <- Gen.listOfN(5, arbitraryEtmpEuRegistrationDetails.arbitrary)
        previousEURegistrationDetails <- Gen.listOfN(5, arbitraryEtmpPreviousEURegistrationDetails.arbitrary)
        onlineMarketPlace <- arbitrary[Boolean]
        websites <- Gen.listOfN(10, arbitraryWebsite.arbitrary)
        contactName <- arbitrary[String]
        businessTelephoneNumber <- arbitrary[String]
        businessEmailId <- arbitrary[String]
        nonCompliantReturns <- Gen.option(arbitrary[String])
        nonCompliantPayments <- Gen.option(arbitrary[String])
      } yield
        EtmpSchemeDetails(
          commencementDate,
          firstSaleDate,
          None,
          None,
          euRegistrationDetails,
          previousEURegistrationDetails,
          onlineMarketPlace,
          websites,
          contactName,
          businessTelephoneNumber,
          businessEmailId,
          nonCompliantReturns,
          nonCompliantPayments
        )
    }
  }

  implicit val arbitraryEtmpDisplauSchemeDetails: Arbitrary[EtmpDisplaySchemeDetails] = {
    Arbitrary {
      for {
        commencementDate <- arbitrary[String]
        firstSaleDate <- Gen.option(arbitrary[String])
        euRegistrationDetails <- Gen.listOfN(5, arbitraryEtmpEuRegistrationDetails.arbitrary)
        previousEURegistrationDetails <- Gen.listOfN(5, arbitraryEtmpPreviousEURegistrationDetails.arbitrary)
        onlineMarketPlace <- arbitrary[Boolean]
        websites <- Gen.listOfN(10, arbitraryWebsite.arbitrary)
        contactName <- arbitrary[String]
        businessTelephoneNumber <- arbitrary[String]
        businessEmailId <- arbitrary[String]
        nonCompliantReturns <- Gen.option(arbitrary[String])
        nonCompliantPayments <- Gen.option(arbitrary[String])
        exclusions <- Gen.listOfN(1, arbitraryEtmpExclusion.arbitrary)
      } yield
        EtmpDisplaySchemeDetails(
          commencementDate,
          firstSaleDate,
          None,
          None,
          euRegistrationDetails,
          previousEURegistrationDetails,
          onlineMarketPlace,
          websites,
          contactName,
          businessTelephoneNumber,
          businessEmailId,
          nonCompliantReturns,
          nonCompliantPayments,
          exclusions
        )
    }
  }

  private def datesBetween(min: LocalDate, max: LocalDate): Gen[LocalDate] = {

    def toMillis(date: LocalDate): Long =
      date.atStartOfDay.atZone(ZoneOffset.UTC).toInstant.toEpochMilli

    Gen.choose(toMillis(min), toMillis(max)).map {
      millis =>
        Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate
    }
  }

  implicit val arbitraryRegistration: Arbitrary[Registration] =
    Arbitrary {
      for {
        vrn <- arbitrary[Vrn]
        name <- arbitrary[String]
        vatDetails <- arbitrary[VatDetails]

        contactDetails <- arbitrary[ContactDetails]
        bankDetails <- arbitrary[BankDetails]
        commencementDate <- datesBetween(LocalDate.of(2021, 7, 1), LocalDate.now)
        isOnlineMarketplace <- arbitrary[Boolean]
        adminUse <- arbitrary[AdminUse]
      } yield Registration(vrn, name, Nil, vatDetails, Nil, contactDetails, Nil, commencementDate, Nil, bankDetails, isOnlineMarketplace, None, None, None, None, None, None, None, None, adminUse)
    }

  implicit val arbitraryStandardPeriod: Arbitrary[StandardPeriod] =
    Arbitrary {
      for {
        year <- Gen.choose(2022, 2099)
        quarter <- Gen.oneOf(Quarter.values)
      } yield StandardPeriod(year, quarter)
    }

  implicit lazy val arbitraryDate: Arbitrary[LocalDate] =
    Arbitrary {
      datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2023, 12, 31))
    }

  implicit lazy val arbitraryExcludedTrader: Arbitrary[ExcludedTrader] =
    Arbitrary {
      for {
        vrn <- arbitraryVrn.arbitrary
        exclusionReason <- Gen.oneOf(ExclusionReason.values)
        effectiveDate <- arbitraryDate.arbitrary
        quarantined <- arbitrary[Boolean]
      } yield ExcludedTrader(
        vrn = vrn,
        exclusionReason = exclusionReason,
        effectiveDate = effectiveDate,
        quarantined = quarantined
      )
    }
    
  implicit lazy val arbitraryFailedEnrolmentStatus: Arbitrary[EnrolmentStatus] =
    Arbitrary {
      Gen.oneOf(
        EnrolmentStatus.Failure,
        EnrolmentStatus.Enrolled,
        EnrolmentStatus.EnrolmentError,
        EnrolmentStatus.AuthRefreshed
      )
    }
}
