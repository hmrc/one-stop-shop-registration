package models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}

class AddressSpec extends AnyFreeSpec with Matchers {

  "Address" - {

    "must serialise and deserialise, with GB as the country code, to and from a UK address" - {

      "with all optional fields present" in {

        val address: Address = UkAddress("line 1", Some("line 2"), "town", Some("county"), "AA11 1AA")

        val expectedJson = Json.obj(
          "line1"       -> "line 1",
          "line2"       -> "line 2",
          "townOrCity"  -> "town",
          "county"      -> "county",
          "postCode"    -> "AA11 1AA",
          "countryCode" -> "GB"
        )

        Json.toJson(address) mustEqual expectedJson
        expectedJson.validate[Address] mustEqual JsSuccess(address)
      }

      "with all optional fields missing" in {

        val address: Address = UkAddress("line 1", None, "town", None, "AA11 1AA")

        val expectedJson = Json.obj(
          "line1"       -> "line 1",
          "townOrCity"  -> "town",
          "postCode"    -> "AA11 1AA",
          "countryCode" -> "GB"
        )

        Json.toJson(address) mustEqual expectedJson
        expectedJson.validate[Address] mustEqual JsSuccess(address)
      }
    }

    "must serialise from a DES address" - {

      "with all optional fields present" in {

        val address: Address = DesAddress("line 1", Some("line 2"), Some("line 3"), Some("line 4"), Some("line 5"), Some("postcode"), "CC")

        val expectedJson = Json.obj(
          "line1"       -> "line 1",
          "line2"       -> "line 2",
          "line3"       -> "line 3",
          "line4"       -> "line 4",
          "line5"       -> "line 5",
          "postCode"    -> "postcode",
          "countryCode" -> "CC"
        )

        Json.toJson(address) mustEqual expectedJson
        expectedJson.validate[Address] mustEqual JsSuccess(address)
      }

      "with all optional fields missing" in {

        val address: Address = DesAddress("line 1", None, None, None, None, None, "CC")

        val expectedJson = Json.obj(
          "line1"       -> "line 1",
          "countryCode" -> "CC"
        )

        Json.toJson(address) mustEqual expectedJson
        expectedJson.validate[Address] mustEqual JsSuccess(address)
      }
    }
  }
}
