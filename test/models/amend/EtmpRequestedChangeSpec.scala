package models.amend

import base.BaseSpec
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}


class EtmpRequestedChangeSpec extends BaseSpec  {

  "EtmpRequestedChange" - {

    "must serialise/deserialise to and from EtmpRequestedChange" in {

      val etmpRequestedChange = EtmpRequestedChange(
        tradingName = true,
        fixedEstablishment = false,
        contactDetails = false,
        bankDetails = true,
        reRegistration = false,
        exclusion = true
      )

      val expectedJson = Json.obj(
        "reRegistration" -> false,
        "tradingName" -> true,
        "bankDetails" -> true,
        "fixedEstablishment" -> false,
        "exclusion" -> true,
        "contactDetails" -> false
      )

      Json.toJson(etmpRequestedChange) mustBe expectedJson
      expectedJson.validate[EtmpRequestedChange] mustBe JsSuccess(etmpRequestedChange)
    }

    "when values are absent" in {

      val expectedJson = Json.obj()

      expectedJson.validate[EtmpRequestedChange] mustBe a[JsError]
    }

    "when invalid values" in {

      val expectedJson = Json.obj(
        "reRegistration" -> 12345,
        "tradingName" -> true,
        "bankDetails" -> true,
        "fixedEstablishment" -> false,
        "exclusion" -> true,
        "contactDetails" -> false
      )

      expectedJson.validate[EtmpRequestedChange] mustBe a[JsError]
    }

    "when null values" in {

      val expectedJson = Json.obj(
        "reRegistration" -> JsNull,
        "tradingName" -> true,
        "bankDetails" -> true,
        "fixedEstablishment" -> false,
        "exclusion" -> true,
        "contactDetails" -> false
      )

      expectedJson.validate[EtmpRequestedChange] mustBe a[JsError]
    }
  }
}
