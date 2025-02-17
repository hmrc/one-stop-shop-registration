package models

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

class EuTaxRegistrationSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators {

  "EU Tax Registration" - {

    "must serialise and deserialise from / to a Registration with Fixed Establishment" in {

      forAll(arbitrary[Country], arbitrary[TradeDetails], arbitrary[EuTaxIdentifier]) {
        case (country, fixedEstablishment, taxRef) =>

          val euRegistration = RegistrationWithFixedEstablishment(country, taxRef, fixedEstablishment)

          val json = Json.toJson(euRegistration)
          json.validate[EuTaxRegistration] mustEqual JsSuccess(euRegistration)
      }
    }

    "must serialise and deserialise from / to a Registration without Fixed Establishment" in {

      forAll(arbitrary[Country]) {
        country =>
          val euRegistration = RegistrationWithoutTaxId(country)

          val json = Json.toJson(euRegistration)
          json.validate[EuTaxRegistration] mustEqual JsSuccess(euRegistration)
      }
    }
  }

  "Encrypted EU Tax Registration" - {

    "must serialise and deserialise from / to a Registration with Fixed Establishment" in {

      val gen = for {
        country        <- arbitrary[EncryptedCountry]
        name           <- arbitrary[String]
        line1          <- arbitrary[String]
        town           <- arbitrary[String]
        identifierType <- arbitrary[String]
        taxRef         <- arbitrary[String]
        addressCountry <- arbitrary[Country]
      } yield (
        country,
        EncryptedTradeDetails(name, EncryptedInternationalAddress(line1, None, town, None, None, addressCountry)),
        EncryptedEuTaxIdentifier(identifierType, taxRef)
      )

      forAll(gen) {
        case (country, fixedEstablishment, taxRef) =>

          val euRegistration = EncryptedRegistrationWithFixedEstablishment(country, taxRef, fixedEstablishment)

          val json = Json.toJson(euRegistration)
          json.validate[EncryptedEuTaxRegistration] mustEqual JsSuccess(euRegistration)
      }
    }

    "must serialise and deserialise from / to a Registration without Fixed Establishment" in {

      forAll(arbitrary[EncryptedCountry]) {
        country =>
          val euRegistration = EncryptedRegistrationWithoutTaxId(country)

          val json = Json.toJson(euRegistration)
          json.validate[EncryptedEuTaxRegistration] mustEqual JsSuccess(euRegistration)
      }
    }
  }

  "EuVatRegistration" - {

    "must deserialise/serialise to and from EuVatRegistration" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "CZ" ,
          "name" -> "Croatia"
        ),
        "vatNumber" -> "123456789"
      )

      val expectedResult = EuVatRegistration(
        country = Country("CZ", "Croatia"),
        vatNumber = "123456789"

      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EuVatRegistration] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[EuVatRegistration] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "CZ" ,
          "name" -> "Croatia"
        ),
        "vatNumber" -> 12345
      )

      json.validate[EuVatRegistration] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "CZ" ,
          "name" -> JsNull
        ),
        "vatNumber" -> "123456789"
      )

      json.validate[EuVatRegistration] mustBe a[JsError]
    }
  }

  "EncryptedEuVatRegistration" - {

    "must deserialise/serialise to and from EncryptedEuVatRegistration" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "CZ" ,
          "name" -> "Croatia"
        ),
        "vatNumber" -> "123456789"
      )

      val expectedResult = EncryptedEuVatRegistration(
        country = EncryptedCountry("CZ", "Croatia"),
        vatNumber = "123456789"

      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EncryptedEuVatRegistration] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[EncryptedEuVatRegistration] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "CZ" ,
          "name" -> "Croatia"
        ),
        "vatNumber" -> 12345
      )

      json.validate[EncryptedEuVatRegistration] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "CZ" ,
          "name" -> "Croatia"
        ),
        "vatNumber" -> JsNull
      )

      json.validate[EncryptedEuVatRegistration] mustBe a[JsError]
    }
  }

  "RegistrationWithoutFixedEstablishmentWithTradeDetails" - {

    "must deserialise/serialise to and from RegistrationWithoutFixedEstablishmentWithTradeDetails" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "FR",
          "name" -> "France"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "other",
          "value" -> "123456789"
        ),
        "tradeDetails" -> Json.obj(
          "tradingName" -> "French Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> "FR",
              "name" -> "France"
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      val expectedResult = RegistrationWithoutFixedEstablishmentWithTradeDetails(
        country = Country("FR", "France"),
        taxIdentifier = EuTaxIdentifier(
          identifierType = EuTaxIdentifierType.Other,
          value = "123456789"
        ),
        tradeDetails = TradeDetails(
          tradingName = "French Trading Name",
          address = InternationalAddress(
            line1 = "Line 1",
            line2 = Some("Line 2"),
            townOrCity = "Town",
            stateOrRegion = Some("Region"),
            postCode = Some("Postcode"),
            country = Country("FR", "France")
          )
        )
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[RegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[RegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "FR",
          "name" -> "France"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "other",
          "value" -> "123456789"
        ),
        "tradeDetails" -> Json.obj(
          "tradingName" -> "French Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> 12345,
              "name" -> "France"
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      json.validate[RegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "FR",
          "name" -> "France"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "other",
          "value" -> "123456789"
        ),
        "tradeDetails" -> Json.obj(
          "tradingName" -> "French Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> JsNull,
              "name" -> "France"
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      json.validate[RegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe a[JsError]
    }
  }

  "EncryptedRegistrationWithoutFixedEstablishmentWithTradeDetails" - {

    "must deserialise/serialise to and from EncryptedRegistrationWithoutFixedEstablishmentWithTradeDetails" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "FR",
          "name" -> "France"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "other",
          "value" -> "123456789"
        ),
        "tradeDetails" -> Json.obj(
          "tradingName" -> "French Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> "FR",
              "name" -> "France"
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      val expectedResult = EncryptedRegistrationWithoutFixedEstablishmentWithTradeDetails(
        country = EncryptedCountry("FR", "France"),
        taxIdentifier = EncryptedEuTaxIdentifier(
          identifierType = "other",
          value = "123456789"
        ),
        tradeDetails = EncryptedTradeDetails(
          tradingName = "French Trading Name",
          address = EncryptedInternationalAddress(
            line1 = "Line 1",
            line2 = Some("Line 2"),
            townOrCity = "Town",
            stateOrRegion = Some("Region"),
            postCode = Some("Postcode"),
            country = Country("FR", "France")
          )
        )
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EncryptedRegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[EncryptedRegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "FR",
          "name" -> "France"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "other",
          "value" -> "123456789"
        ),
        "tradeDetails" -> Json.obj(
          "tradingName" -> "French Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> 12345,
              "name" -> "France"
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      json.validate[EncryptedRegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "FR",
          "name" -> "France"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "other",
          "value" -> "123456789"
        ),
        "tradeDetails" -> Json.obj(
          "tradingName" -> "French Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> "FR",
              "name" -> JsNull
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      json.validate[EncryptedRegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe a[JsError]
    }
  }

  "RegistrationWithFixedEstablishment" - {

    "must deserialise/serialise to and from RegistrationWithFixedEstablishment" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "ES",
          "name" -> "Spain"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "fixedEstablishment" -> Json.obj(
          "tradingName" -> "Spanish Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> "ES",
              "name" -> "Spain"
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      val expectedResult = RegistrationWithFixedEstablishment(
        country = Country("ES", "Spain"),
        taxIdentifier = EuTaxIdentifier(
          identifierType = EuTaxIdentifierType.Vat,
          value = "123456789"
        ),
        fixedEstablishment = TradeDetails(
          tradingName = "Spanish Trading Name",
          address = InternationalAddress(
            line1 = "Line 1",
            line2 = Some("Line 2"),
            townOrCity = "Town",
            stateOrRegion = Some("Region"),
            postCode = Some("Postcode"),
            country = Country("ES", "Spain")
          )
        )
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[RegistrationWithFixedEstablishment] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[RegistrationWithFixedEstablishment] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> 12345,
          "name" -> "Spain"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "fixedEstablishment" -> Json.obj(
          "tradingName" -> "Spanish Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> "ES",
              "name" -> "Spain"
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      json.validate[RegistrationWithFixedEstablishment] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "ES",
          "name" -> JsNull
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "fixedEstablishment" -> Json.obj(
          "tradingName" -> "Spanish Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> "ES",
              "name" -> "Spain"
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      json.validate[RegistrationWithFixedEstablishment] mustBe a[JsError]
    }
  }

  "EncryptedRegistrationWithFixedEstablishment" - {

    "must deserialise/serialise to and from EncryptedRegistrationWithFixedEstablishment" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "ES",
          "name" -> "Spain"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "fixedEstablishment" -> Json.obj(
          "tradingName" -> "Spanish Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> "ES",
              "name" -> "Spain"
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      val expectedResult = EncryptedRegistrationWithFixedEstablishment(
        country = EncryptedCountry("ES", "Spain"),
        taxIdentifier = EncryptedEuTaxIdentifier(
          identifierType = "vat",
          value = "123456789"
        ),
        fixedEstablishment = EncryptedTradeDetails(
          tradingName = "Spanish Trading Name",
          address = EncryptedInternationalAddress(
            line1 = "Line 1",
            line2 = Some("Line 2"),
            townOrCity = "Town",
            stateOrRegion = Some("Region"),
            postCode = Some("Postcode"),
            country = Country("ES", "Spain")
          )
        )
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EncryptedRegistrationWithFixedEstablishment] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[EncryptedRegistrationWithFixedEstablishment] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> 12345,
          "name" -> "Spain"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "fixedEstablishment" -> Json.obj(
          "tradingName" -> "Spanish Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> "ES",
              "name" -> "Spain"
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      json.validate[EncryptedRegistrationWithFixedEstablishment] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "ES",
          "name" -> JsNull
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "fixedEstablishment" -> Json.obj(
          "tradingName" -> "Spanish Trading Name",
          "address" -> Json.obj(
            "postCode" -> "Postcode",
            "country" -> Json.obj(
              "code" -> "ES",
              "name" -> "Spain"
            ),
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "line2" -> "Line 2",
            "stateOrRegion" -> "Region"
          )
        )
      )

      json.validate[EncryptedRegistrationWithFixedEstablishment] mustBe a[JsError]
    }
  }

  "RegistrationWithoutTaxId" - {

    "must deserialise/serialise to and from RegistrationWithoutTaxId" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "BE",
          "name" -> "Belgium"
        )
      )

      val expectedResult = RegistrationWithoutTaxId(
        country = Country("BE", "Belgium")
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[RegistrationWithoutTaxId] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[RegistrationWithoutTaxId] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> 12345,
          "name" -> "Belgium"
        )
      )

      json.validate[RegistrationWithoutTaxId] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "BE",
          "name" -> JsNull
        )
      )

      json.validate[RegistrationWithoutTaxId] mustBe a[JsError]
    }
  }

  "EncryptedRegistrationWithoutTaxId" - {

    "must deserialise/serialise to and from EncryptedRegistrationWithoutTaxId" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "BE",
          "name" -> "Belgium"
        )
      )

      val expectedResult = EncryptedRegistrationWithoutTaxId(
        country = EncryptedCountry("BE", "Belgium")

      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EncryptedRegistrationWithoutTaxId] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[EncryptedRegistrationWithoutTaxId] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "BE",
          "name" -> 12345
        )
      )

      json.validate[EncryptedRegistrationWithoutTaxId] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> JsNull,
          "name" -> "Belgium"
        )
      )

      json.validate[EncryptedRegistrationWithoutTaxId] mustBe a[JsError]

    }
  }
}