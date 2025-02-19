package models.etmp

import base.BaseSpec
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}


class EtmpAdministrationSpec extends BaseSpec {

  "EtmpAdministration" - {

    "must deserialise/serialise to and from EtmpAdministration create" in {

      val json = Json.obj(
        "messageType" -> "OSSSubscriptionCreate",
        "regimeID" -> "OSS"
      )

      val expectedResult = EtmpAdministration(
        messageType = EtmpMessageType.OSSSubscriptionCreate,
        regimeID = "OSS"
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EtmpAdministration] mustBe JsSuccess(expectedResult)
    }

    "must deserialise/serialise to and from EtmpAdministration amend" in {

      val json = Json.obj(
        "messageType" -> "OSSSubscriptionAmend",
        "regimeID" -> "OSS"
      )

      val expectedResult = EtmpAdministration(
        messageType = EtmpMessageType.OSSSubscriptionAmend,
        regimeID = "OSS"
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EtmpAdministration] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[EtmpAdministration] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "messageType" -> 12345,
        "regimeID" -> "OSS"
      )

      json.validate[EtmpAdministration] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "messageType" -> JsNull,
        "regimeID" -> "OSS"
      )

      json.validate[EtmpAdministration] mustBe a[JsError]
    }
  }
}
