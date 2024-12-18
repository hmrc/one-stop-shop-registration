package models

import base.BaseSpec
import models.Quarter.*
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsSuccess, Json}



class StandardPeriodSpec extends BaseSpec with ScalaCheckPropertyChecks {


  "StandardPeriod" - {
      "must serialise to and deserialise from JSON" in {
        val period = StandardPeriod(2021, Q1)
        val json = Json.toJson(period)

        val expectedJson = Json.obj(
          "year" -> 2021,
          "quarter" -> "Q1"
        )

        json mustBe expectedJson
        expectedJson.validate[StandardPeriod] mustEqual JsSuccess(period)
      }
    }


  }


