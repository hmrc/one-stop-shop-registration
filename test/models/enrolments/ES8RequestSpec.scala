package models.enrolments

import base.BaseSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

class ES8RequestSpec extends BaseSpec with Matchers {

  "ES8Request" - {

    "must deserialise/serialise to and from ES8Request" in {

      val json = Json.obj(
        "verifiers"-> Json.arr(
          Json.obj(
            "OSSRegistrationDate" -> "20241017"
          )
        ),
        "friendlyName" -> "OSS Subscription",
        "action" -> "enrolAndActivate",
        "userId" -> "user-1234",
        "type" -> "principal"
      )

      val expectedResult = ES8Request(
        userId = "user-1234",
        friendlyName = "OSS Subscription",
        `type` = "principal",
        action = "enrolAndActivate",
        verifiers = Seq(Map("OSSRegistrationDate" -> "20241017"))
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[ES8Request] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[ES8Request] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "verifiers"-> Json.arr(
          Json.obj(
            "OSSRegistrationDate" -> "20241017"
          )
        ),
        "friendlyName" -> "OSS Subscription",
        "action" -> "enrolAndActivate",
        "userId" -> "user-1234",
        "type" -> 12345
      )

      json.validate[ES8Request] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "verifiers"-> Json.arr(
          Json.obj(
            "OSSRegistrationDate" -> "20241017"
          )
        ),
        "friendlyName" -> "OSS Subscription",
        "action" -> "enrolAndActivate",
        "userId" -> "user-1234",
        "type" -> JsNull
      )

      json.validate[ES8Request] mustBe a[JsError]
    }
  }
}
