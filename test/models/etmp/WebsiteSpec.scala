package models.etmp

import base.BaseSpec
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}


class WebsiteSpec extends BaseSpec {

  "Website" - {

    "must serialize to JSON correctly" in {

      val website = Website("http://example.com")

      val expectedJson = Json.obj(
        "websiteAddress" -> "http://example.com"
      )

      Json.toJson(website) mustBe expectedJson
    }

    "must deserialize from JSON correctly" in {

      val json = Json.obj(
        "websiteAddress" -> "http://example.com"
      )

      val expectedResult = Website("http://example.com")

      json.validate[Website] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[Website] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj("websiteAddress" -> 12345)

      json.validate[Website] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj("websiteAddress" -> JsNull)

      json.validate[Website] mustBe a[JsError]
    }
  }
}
