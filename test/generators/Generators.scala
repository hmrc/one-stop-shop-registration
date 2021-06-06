package generators

import models.{Country, EuTaxIdentifier, EuTaxIdentifierType, FixedEstablishment, FixedEstablishmentAddress}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait Generators {

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
}
