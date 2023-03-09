package models.etmp

import base.BaseSpec
import play.api.libs.json.{JsSuccess, Json}

class EtmpPreviousEURegistrationDetailsSpec extends BaseSpec {

  "EtmpPreviousEURegistrationDetails" - {

    "must deserialise" - {

      "when all optional fields are present" in {

        val json = Json.obj(
          "issuedBy" -> "ES",
          "registrationNumber" -> "ES123456789",
          "schemeType" -> "IOSS with intermediary",
          "intermediaryNumber" -> "IN7241234567"
        )

        val expectedResult = EtmpPreviousEURegistrationDetails(
          issuedBy = "ES",
          registrationNumber = "ES123456789",
          schemeType = SchemeType.IOSSWithIntermediary,
          intermediaryNumber = Some("IN7241234567")
        )

        json.validate[EtmpPreviousEURegistrationDetails] mustEqual JsSuccess(expectedResult)
      }


      "when all optional fields are absent" in {

        val json = Json.obj(
          "issuedBy" -> "ES",
          "registrationNumber" -> "ES123456789",
          "schemeType" -> "IOSS with intermediary"
        )

        val expectedResult = EtmpPreviousEURegistrationDetails(
          issuedBy = "ES",
          registrationNumber = "ES123456789",
          schemeType = SchemeType.IOSSWithIntermediary,
          intermediaryNumber = None
        )

        json.validate[EtmpPreviousEURegistrationDetails] mustEqual JsSuccess(expectedResult)
      }
    }
  }
}
