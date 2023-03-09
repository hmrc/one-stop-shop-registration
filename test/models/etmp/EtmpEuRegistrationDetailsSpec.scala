package models.etmp

import base.BaseSpec
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

    //TODO - create method???
    ".create" - {

      "must ???"
    }
  }
}
