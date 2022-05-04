package generators

import crypto.EncryptedValue
import models.requests.SaveForLaterRequest
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.domain.Vrn

import java.time.{Instant, LocalDate}

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
        char7      <- Gen.oneOf(Gen.alphaUpperChar, Gen.choose(2, 9))
        char8 <- Gen.oneOf(
          Gen.choose(asciiCodeForA, asciiCodeForN).map(_.toChar),
          Gen.choose(asciiCodeForP, asciiCodeForZ).map(_.toChar),
          Gen.choose(0, 9)
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
      ).map(v => Iban(v).right.get)
    }

  implicit lazy val arbitraryEncryptedCountry: Arbitrary[EncryptedCountry] =
    Arbitrary {
      for {
        code <- arbitrary[EncryptedValue]
        name <- arbitrary[EncryptedValue]
      } yield EncryptedCountry(code, name)
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
        address          <- arbitrary[Address]
        partOfVatGroup   <- arbitrary[Boolean]
        source           <- arbitrary[VatDetailSource]
      } yield VatDetails(registrationDate, address, partOfVatGroup, source)
    }

  implicit val arbitraryVatDetailSource: Arbitrary[VatDetailSource] =
    Arbitrary(
      Gen.oneOf(VatDetailSource.values)
    )

  implicit lazy val arbitraryPreviousRegistration: Arbitrary[PreviousRegistration] =
    Arbitrary {
      for {
        country   <- arbitrary[Country]
        vatNumber <- Gen.listOfN(11, Gen.alphaChar).map(_.mkString)
      } yield PreviousRegistration(country, vatNumber)
    }

  implicit lazy val arbitraryEuTaxRegistration: Arbitrary[EuTaxRegistration] =
    Arbitrary {
      Gen.oneOf(arbitrary[RegistrationWithoutFixedEstablishment], arbitrary[RegistrationWithFixedEstablishment])
    }

  implicit lazy val arbitraryEuVatRegistration: Arbitrary[RegistrationWithoutFixedEstablishment] =
    Arbitrary {
      for {
        country   <- arbitrary[Country]
        taxIdentifier      <- arbitrary[EuTaxIdentifier]
        sendsGoods <- arbitrary[Boolean]
        tradingName <- arbitrary[String]
        address <- arbitraryInternationalAddress.arbitrary
      } yield RegistrationWithoutFixedEstablishment(country,
        taxIdentifier,
        Some(sendsGoods)
      )
    }

  implicit lazy val arbitraryEuVatRegistrationSendingGoods: Arbitrary[RegistrationSendingGoods] =
    Arbitrary {
      for {
        country   <- arbitrary[Country]
        taxIdentifier      <- arbitrary[EuTaxIdentifier]
        sendsGoods <- arbitrary[Boolean]
        tradingName <- arbitrary[String]
        address <- arbitraryInternationalAddress.arbitrary
      } yield RegistrationSendingGoods(country,
        taxIdentifier,
        sendsGoods,
        tradingName,
        address
      )
    }

  implicit lazy val arbitraryRegistrationWithFixedEstablishment: Arbitrary[RegistrationWithFixedEstablishment] =
    Arbitrary {
      for {
        country            <- arbitrary[Country]
        taxIdentifier      <- arbitrary[EuTaxIdentifier]
        fixedEstablishment <- arbitrary[FixedEstablishment]
      } yield RegistrationWithFixedEstablishment(country, taxIdentifier, fixedEstablishment)
    }

  implicit lazy val arbitraryRegistrationWithoutFixedEstablistment: Arbitrary[RegistrationWithoutTaxId] =
    Arbitrary {
      arbitrary[Country].map(c => RegistrationWithoutTaxId(c))
    }

  implicit lazy val arbitraryUkAddress: Arbitrary[UkAddress] =
    Arbitrary {
      for {
        line1      <- arbitrary[String]
        line2      <- Gen.option(arbitrary[String])
        townOrCity <- arbitrary[String]
        county     <- Gen.option(arbitrary[String])
        postCode   <- arbitrary[String]
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
        line1         <- arbitrary[String]
        line2         <- Gen.option(arbitrary[String])
        townOrCity    <- arbitrary[String]
        stateOrRegion <- Gen.option(arbitrary[String])
        postCode      <- Gen.option(arbitrary[String])
        country       <- arbitrary[Country]
      } yield InternationalAddress(line1, line2, townOrCity, stateOrRegion, postCode, country)
    }

  implicit lazy val arbitraryDesAddress: Arbitrary[DesAddress] =
    Arbitrary {
      for {
        line1       <- arbitrary[String]
        line2       <- Gen.option(arbitrary[String])
        line3       <- Gen.option(arbitrary[String])
        line4       <- Gen.option(arbitrary[String])
        line5       <- Gen.option(arbitrary[String])
        postCode    <- Gen.option(arbitrary[String])
        countryCode <- Gen.listOfN(2, Gen.alphaChar).map(_.mkString)
      } yield DesAddress(line1, line2, line3, line4, line5, postCode, countryCode)
    }

  implicit lazy val arbitraryBankDetails: Arbitrary[BankDetails] =
    Arbitrary {
      for {
        accountName <- arbitrary[String]
        bic         <- Gen.option(arbitrary[Bic])
        iban        <- arbitrary[Iban]
      } yield BankDetails(accountName, bic, iban)
    }

  implicit val arbitraryEuTaxIdentifierType: Arbitrary[EuTaxIdentifierType] =
    Arbitrary {
      Gen.oneOf(EuTaxIdentifierType.values)
    }

  implicit val arbitraryEuTaxIdentifier: Arbitrary[EuTaxIdentifier] =
    Arbitrary {
      for {
        identifierType <- arbitrary[EuTaxIdentifierType]
        value          <- arbitrary[Int].map(_.toString)
      } yield EuTaxIdentifier(identifierType, value)
    }

  implicit lazy val arbitraryFixedEstablishment: Arbitrary[FixedEstablishment] =
    Arbitrary {
      for {
        tradingName <- arbitrary[String]
        address     <- arbitrary[InternationalAddress]
      } yield FixedEstablishment(tradingName, address)
    }


  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
     for {
       char1 <- Gen.alphaUpperChar
       char2 <- Gen.alphaUpperChar
       name  <- arbitrary[String]
     } yield Country(s"$char1$char2", name)
    }

  implicit lazy val arbitraryBusinessContactDetails: Arbitrary[ContactDetails] =
    Arbitrary {
      for {
        fullName        <- arbitrary[String]
        telephoneNumber <- arbitrary[String]
        emailAddress    <- arbitrary[String]
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
        vrn, data, None, now)
    }

  implicit val arbitrarySaveForLaterRequest: Arbitrary[SaveForLaterRequest] =
    Arbitrary {
      for {
        vrn <- arbitrary[Vrn]
        data = JsObject(Seq(
          "test" -> Json.toJson("test")
        ))
      } yield SaveForLaterRequest(vrn, data, None)
    }
}
