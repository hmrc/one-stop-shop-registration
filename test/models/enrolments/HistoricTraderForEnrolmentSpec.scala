package models.enrolments

import base.BaseSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}


class HistoricTraderForEnrolmentSpec extends BaseSpec with Matchers {

  "HistoricTraderForEnrolment" - {

    "must deserialise/serialise to and from HistoricTraderForEnrolment" in {

      val json = Json.obj(
        "vrn" -> "123456789",
        "groupId" -> "153b9659-c686-4f63-ba70-c32879eed625",
        "userId" -> "987654321"
      )

      val expectedResult = HistoricTraderForEnrolment(
        vrn = vrn,
        groupId = "153b9659-c686-4f63-ba70-c32879eed625",
        userId = "987654321"
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[HistoricTraderForEnrolment] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[HistoricTraderForEnrolment] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "vrn" -> 123456789,
        "groupId" -> "153b9659-c686-4f63-ba70-c32879eed625",
        "userId" -> "987654321"
      )

      json.validate[HistoricTraderForEnrolment] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "vrn" -> JsNull,
        "groupId" -> "153b9659-c686-4f63-ba70-c32879eed625",
        "userId" -> "987654321"
      )

      json.validate[HistoricTraderForEnrolment] mustBe a[JsError]
    }
  }
}
