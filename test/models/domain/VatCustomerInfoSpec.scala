package models.domain

import base.BaseSpec
import crypto.EncryptedValue
import models.{DesAddress, EncryptedDesAddress}
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class VatCustomerInfoSpec extends BaseSpec {

  "VatCustomerInfo" - {

    "must serialise and deserialise correctly" - {
      "when all optional fields are present" in {

        val json = Json.obj(
          "address" -> Json.obj(
            "line1" -> "line 1",
            "line2" -> "line 2",
            "line3" -> "line 3",
            "line4" -> "line 4",
            "line5" -> "line 5",
            "postCode" -> "postcode",
            "countryCode" -> "CC"
          ),
          "registrationDate" -> "2023-12-06",
          "partOfVatGroup" -> true,
          "organisationName" -> "Test Organisation",
          "singleMarketIndicator" -> false
        )

        val expectedResult = VatCustomerInfo(
          address = DesAddress("line 1", Some("line 2"), Some("line 3"), Some("line 4"), Some("line 5"), Some("postcode"), "CC"),
          registrationDate = Some(LocalDate.of(2023, 12, 6)),
          partOfVatGroup = Some(true),
          organisationName = Some("Test Organisation"),
          singleMarketIndicator = Some(false)
        )

        json.validate[VatCustomerInfo] mustEqual JsSuccess(expectedResult)
      }

      "when all optional fields are absent" in {

        val json = Json.obj(
          "address" -> Json.obj(
            "line1" -> "line 1",
            "countryCode" -> "CC"
          )
        )

        val expectedResult = VatCustomerInfo(
          address = DesAddress("line 1", None, None, None, None, None, "CC"),
          registrationDate = None,
          partOfVatGroup = None,
          organisationName = None,
          singleMarketIndicator = None
        )

        json.validate[VatCustomerInfo] mustEqual JsSuccess(expectedResult)
      }
    }
  }

  "EncryptedVatCustomerInfo" - {

    "must serialise and deserialise correctly" - {
      "when all optional fields are present" in {

        val json = Json.obj(
          "address" -> Json.obj(
            "line1" -> "line 1",
            "line2" -> "line 2",
            "line3" -> "line 3",
            "line4" -> "line 4",
            "line5" -> "line 5",
            "postCode" -> "postcode",
            "countryCode" -> "CC"
          ),
          "registrationDate" -> "2023-12-06",
          "partOfVatGroup" -> Json.obj("value" -> "encryptedValue", "nonce" -> "nonce"),
          "organisationName" -> Json.obj("value" -> "encryptedOrgName", "nonce" -> "nonce"),
          "singleMarketIndicator" -> Json.obj("value" -> "encryptedIndicator", "nonce" -> "nonce")
        )

        val expectedResult = EncryptedVatCustomerInfo(
          address = EncryptedDesAddress(
            line1 = "line 1",
            line2 = Some("line 2"),
            line3 = Some("line 3"),
            line4 = Some("line 4"),
            line5 = Some("line 5"),
            postCode = Some("postcode"),
            countryCode = "CC"
          ),
          registrationDate = Some(LocalDate.of(2023, 12, 6)),
          partOfVatGroup = Some(EncryptedValue("encryptedValue", "nonce")),
          organisationName = Some(EncryptedValue("encryptedOrgName", "nonce")),
          singleMarketIndicator = Some(EncryptedValue("encryptedIndicator", "nonce"))
        )

        json.validate[EncryptedVatCustomerInfo] mustEqual JsSuccess(expectedResult)
        Json.toJson(expectedResult) mustEqual json
      }

      "when all optional fields are absent" in {

        val json = Json.obj(
          "address" -> Json.obj(
            "line1" -> "line 1",
            "countryCode" -> "CC"
          ),
        )

        val expectedResult = EncryptedVatCustomerInfo(
          address = EncryptedDesAddress("line 1", None, None, None, None, None, "CC"),
          registrationDate = None,
          partOfVatGroup = None,
          organisationName = None,
          singleMarketIndicator = None,
        )

        json.validate[EncryptedVatCustomerInfo] mustEqual JsSuccess(expectedResult)
      }

    }
  }
}
