package generators

import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

import java.time.LocalDate

trait Generators {

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
      Gen.oneOf(arbitrary[EuVatRegistration], arbitrary[RegistrationWithFixedEstablishment])
    }

  implicit lazy val arbitraryEuVatRegistration: Arbitrary[EuVatRegistration] =
    Arbitrary {
      for {
        country   <- arbitrary[Country]
        vatNumber <- Gen.listOfN(9, Gen.numChar).map(_.mkString)
      } yield EuVatRegistration(country, vatNumber)
    }

  implicit lazy val arbitraryRegistrationWithFixedEstablishment: Arbitrary[RegistrationWithFixedEstablishment] =
    Arbitrary {
      for {
        country            <- arbitrary[Country]
        taxIdentifier      <- arbitrary[EuTaxIdentifier]
        fixedEstablishment <- arbitrary[FixedEstablishment]
      } yield RegistrationWithFixedEstablishment(country, taxIdentifier, fixedEstablishment)
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
        bic <- Gen.option(Gen.listOfN(11, Gen.alphaNumChar).map(_.mkString))
        ibanChars <- Gen.choose(5, 34)
        iban <- Gen.listOfN(ibanChars, Gen.oneOf(Gen.alphaChar, Gen.numChar))
      } yield BankDetails(accountName, bic, iban.mkString)
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

  implicit lazy val arbitraryFixedEstablishmentAddress: Arbitrary[FixedEstablishmentAddress] =
    Arbitrary {
      for {
        line1      <- arbitrary[String]
        line2      <- Gen.option(arbitrary[String])
        townOrCity <- arbitrary[String]
        county     <- Gen.option(arbitrary[String])
        postCode   <- Gen.option(arbitrary[String])
      } yield FixedEstablishmentAddress(line1, line2, townOrCity, county, postCode)
    }

  implicit lazy val arbitraryFixedEstablishment: Arbitrary[FixedEstablishment] =
    Arbitrary {
      for {
        tradingName <- arbitrary[String]
        address     <- arbitrary[FixedEstablishmentAddress]
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
}
