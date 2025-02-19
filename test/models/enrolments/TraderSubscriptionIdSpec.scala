package models.enrolments

import base.BaseSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

class TraderSubscriptionIdSpec extends BaseSpec with Matchers {

  "SubscriberRequest" - {

    "must deserialise/serialise to and from TraderSubscriptionId" in {

      val json = Json.obj(
        "vrn" -> "123456789",
        "subscriptionId" -> "123456789"
      )

      val expectedResult = TraderSubscriptionId(
        vrn = "123456789",
        subscriptionId = "123456789"
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[TraderSubscriptionId] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[TraderSubscriptionId] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "vrn" -> 123456789,
        "subscriptionId" -> "123456789"
      )

      json.validate[TraderSubscriptionId] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "vrn" -> JsNull,
        "subscriptionId" -> "123456789"
      )

      json.validate[TraderSubscriptionId] mustBe a[JsError]
    }
  }
}
