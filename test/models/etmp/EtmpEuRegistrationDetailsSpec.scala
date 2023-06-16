package models.etmp

import base.BaseSpec
import models.{Country, EuTaxIdentifier, EuTaxIdentifierType, EuVatRegistration, InternationalAddress, RegistrationWithFixedEstablishment, RegistrationWithoutFixedEstablishmentWithTradeDetails, RegistrationWithoutTaxId, TradeDetails}
import play.api.libs.json.{JsSuccess, Json}

class EtmpEuRegistrationDetailsSpec extends BaseSpec {

  "EtmpEuRegistrationDetails" - {

    "must deserialise" - {

      "when all optional fields are present" in {

        val json = Json.obj(
          "issuedBy" -> "FR",
          "vatNumber" -> "FR123456789",
          "taxIdentificationNumber" -> "123456789",
          "fixedEstablishment" -> true,
          "fixedEstablishmentTradingName" -> "French Trading Company",
          "fixedEstablishmentAddressLine1" -> "Line1",
          "fixedEstablishmentAddressLine2" -> "Line2",
          "townOrCity" -> "Town",
          "regionOrState" -> "Region",
          "postcode" -> "Postcode"
        )

        val expectedResult = EtmpEuRegistrationDetails(
          countryOfRegistration = "FR",
          vatNumber = Some("FR123456789"),
          taxIdentificationNumber = Some("123456789"),
          fixedEstablishment = Some(true),
          tradingName = Some("French Trading Company"),
          fixedEstablishmentAddressLine1 = Some("Line1"),
          fixedEstablishmentAddressLine2 = Some("Line2"),
          townOrCity = Some("Town"),
          regionOrState = Some("Region"),
          postcode = Some("Postcode")
        )

        json.validate[EtmpEuRegistrationDetails](EtmpEuRegistrationDetails.reads) mustEqual JsSuccess(expectedResult)
      }

      "when all optional fields are absent" in {

        val json = Json.obj(
          "issuedBy" -> "BE"
        )

        val expectedResult = EtmpEuRegistrationDetails(
          countryOfRegistration = "BE",
          vatNumber = None,
          taxIdentificationNumber = None,
          fixedEstablishment = None,
          tradingName = None,
          fixedEstablishmentAddressLine1 = None,
          fixedEstablishmentAddressLine2 = None,
          townOrCity = None,
          regionOrState = None,
          postcode = None
        )

        json.validate[EtmpEuRegistrationDetails](EtmpEuRegistrationDetails.reads) mustEqual JsSuccess(expectedResult)
      }
    }

    ".create" - {

      "should create EtmpEuRegistrationDetails from an EuVatRegistration when invoked" in {

        val euVatRegistration = EuVatRegistration(
          country = Country("CZ", "Croatia"),
          vatNumber = "123456789"
        )

        val etmpEuRegistrationDetails = EtmpEuRegistrationDetails(
          countryOfRegistration = "CZ",
          vatNumber = Some("123456789")
        )

        EtmpEuRegistrationDetails.create(euVatRegistration) mustBe etmpEuRegistrationDetails
      }

      "should create EtmpEuRegistrationDetails from a RegistrationWithoutTaxId when invoked" in {

        val euVatRegistration = RegistrationWithoutTaxId(
          country = Country("BE", "Belgium")
        )

        val etmpEuRegistrationDetails = EtmpEuRegistrationDetails(
          countryOfRegistration = "BE"
        )

        EtmpEuRegistrationDetails.create(euVatRegistration) mustBe etmpEuRegistrationDetails
      }

      "should create EtmpEuRegistrationDetails from a RegistrationWithFixedEstablishment when invoked" in {

        val country = Country("ES", "Spain")

        val euVatRegistration = RegistrationWithFixedEstablishment(
          country = country,
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
              country = country
            )
          )
        )

        val etmpEuRegistrationDetails = EtmpEuRegistrationDetails(
          countryOfRegistration = "ES",
          vatNumber = Some("123456789"),
          fixedEstablishment = Some(true),
          tradingName = Some("Spanish Trading Name"),
          fixedEstablishmentAddressLine1 = Some("Line 1"),
          fixedEstablishmentAddressLine2 = Some("Line 2"),
          townOrCity = Some("Town"),
          regionOrState = Some("Region"),
          postcode = Some("Postcode")
        )

        EtmpEuRegistrationDetails.create(euVatRegistration) mustBe etmpEuRegistrationDetails
      }

      "should create EtmpEuRegistrationDetails from a RegistrationWithoutFixedEstablishmentWithTradeDetails when invoked" in {

        val country = Country("FR", "France")

        val euVatRegistration = RegistrationWithoutFixedEstablishmentWithTradeDetails(
          country = country,
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
              country = country
            )
          )
        )

        val etmpEuRegistrationDetails = EtmpEuRegistrationDetails(
          countryOfRegistration = "FR",
          taxIdentificationNumber = Some("123456789"),
          fixedEstablishment = Some(false),
          tradingName = Some("French Trading Name"),
          fixedEstablishmentAddressLine1 = Some("Line 1"),
          fixedEstablishmentAddressLine2 = Some("Line 2"),
          townOrCity = Some("Town"),
          regionOrState = Some("Region"),
          postcode = Some("Postcode")
        )

        EtmpEuRegistrationDetails.create(euVatRegistration) mustBe etmpEuRegistrationDetails
      }
    }
  }
}
