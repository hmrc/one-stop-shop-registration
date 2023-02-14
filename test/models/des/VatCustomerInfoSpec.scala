package models.des

import base.BaseSpec
import models.DesAddress
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class VatCustomerInfoSpec extends BaseSpec {

  "VatCustomerInfo" - {

    "must deserialise" - {

      "when all optional fields are present" in {

        val json = Json.obj(
          "approvedInformation" -> Json.obj(
            "PPOB" -> Json.obj(
              "address" -> Json.obj(
                "line1" -> "line 1",
                "line2" -> "line 2",
                "line3" -> "line 3",
                "line4" -> "line 4",
                "line5" -> "line 5",
                "postCode" -> "postcode",
                "countryCode" -> "CC"
              )
            ),
            "customerDetails" -> Json.obj(
              "effectiveRegistrationDate" -> "2020-01-02",
              "partyType" -> "Z2",
              "organisationName" -> "Foo",
              "singleMarketIndicator" -> false
            )
          )
        )

        val expectedResult = VatCustomerInfo(
          address = DesAddress("line 1", Some("line 2"), Some("line 3"), Some("line 4"), Some("line 5"), Some("postcode"), "CC"),
          registrationDate = Some(LocalDate.of(2020, 1, 2)),
          partOfVatGroup = true,
          organisationName = Some("Foo"),
          singleMarketIndicator = Some(false)
        )

        json.validate[VatCustomerInfo](VatCustomerInfo.desReads) mustEqual JsSuccess(expectedResult)
      }

      "when all optional fields are present and partOfVatGroup is OtherPartyType" in {

        val json = Json.obj(
          "approvedInformation" -> Json.obj(
            "PPOB" -> Json.obj(
              "address" -> Json.obj(
                "line1" -> "line 1",
                "line2" -> "line 2",
                "line3" -> "line 3",
                "line4" -> "line 4",
                "line5" -> "line 5",
                "postCode" -> "postcode",
                "countryCode" -> "CC"
              )
            ),
            "customerDetails" -> Json.obj(
              "effectiveRegistrationDate" -> "2020-01-02",
              "partyType" -> "ZZ",
              "organisationName" -> "Foo",
              "singleMarketIndicator" -> false
            )
          )
        )

        val expectedResult = VatCustomerInfo(
          address = DesAddress("line 1", Some("line 2"), Some("line 3"), Some("line 4"), Some("line 5"), Some("postcode"), "CC"),
          registrationDate = Some(LocalDate.of(2020, 1, 2)),
          partOfVatGroup = false,
          organisationName = Some("Foo"),
          singleMarketIndicator = Some(false)
        )

        json.validate[VatCustomerInfo](VatCustomerInfo.desReads) mustEqual JsSuccess(expectedResult)
      }

      "when all optional fields are absent" in {

        val json = Json.obj(
          "approvedInformation" -> Json.obj(
            "PPOB" -> Json.obj(
              "address" -> Json.obj(
                "line1" -> "line 1",
                "countryCode" -> "CC"
              )
            ),
            "customerDetails" -> Json.obj()
          )
        )

        val expectedResult = VatCustomerInfo(
          address = DesAddress("line 1", None, None, None, None, None, "CC"),
          registrationDate = None,
          partOfVatGroup = false,
          organisationName = None,
          singleMarketIndicator = None
        )

        json.validate[VatCustomerInfo](VatCustomerInfo.desReads) mustEqual JsSuccess(expectedResult)
      }
    }
  }
}
