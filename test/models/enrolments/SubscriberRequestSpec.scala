package models.enrolments

import base.BaseSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

class SubscriberRequestSpec extends BaseSpec with Matchers {

  "SubscriberRequest" - {

    "must deserialise/serialise to and from SubscriberRequest" in {

      val json = Json.obj(
        "serviceName" -> "etmp registration",
        "callback" -> "http://example.com/callback",
        "etmpId" -> "12345-etmp-id"
      )

      val expectedResult = SubscriberRequest(
        serviceName = "etmp registration",
        callback = "http://example.com/callback",
        etmpId = "12345-etmp-id"
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[SubscriberRequest] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[SubscriberRequest] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "serviceName" -> 12345,
        "callback" -> "http://example.com/callback",
        "etmpId" -> "12345-etmp-id"
      )

      json.validate[SubscriberRequest] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "serviceName" -> JsNull,
        "callback" -> "http://example.com/callback",
        "etmpId" -> "12345-etmp-id"
      )

      json.validate[SubscriberRequest] mustBe a[JsError]
    }
  }
}
