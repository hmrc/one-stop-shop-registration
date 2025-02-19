package models.etmp

import base.BaseSpec
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}


class EtmpCustomerIdentificationSpec extends BaseSpec {

  "EtmpCustomerIdentification" - {

    "must deserialise/serialise to and from EtmpCustomerIdentification" in {

      val json = Json.obj(
        "vrn" -> "123456789"
      )

      val expectedResult = EtmpCustomerIdentification(
        vrn = vrn
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EtmpCustomerIdentification] mustBe JsSuccess(expectedResult)
    }

    "when vrn is absent" in {

      val json = Json.obj()

      json.validate[EtmpCustomerIdentification] mustBe a[JsError]
    }

    "when vrn is invalid" in {

      val json = Json.obj(
        "vrn" -> 12345
      )

      json.validate[EtmpCustomerIdentification] mustBe a[JsError]
    }

    "when vrn is null" in {

      val json = Json.obj(
        "vrn" -> JsNull
      )

      json.validate[EtmpCustomerIdentification] mustBe a[JsError]
    }
  }
}
