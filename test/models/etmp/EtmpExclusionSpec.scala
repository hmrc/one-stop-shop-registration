package models.etmp

import base.BaseSpec
import play.api.libs.json.{JsSuccess, Json}

class EtmpExclusionSpec extends BaseSpec {

  "EtmpExclusion" - {

    "must deserialise/serialise to and from EtmpExclusion" in {

      val json = Json.obj(
        "exclusionReason" -> "1",
        "effectiveDate" -> "2024-02-25",
        "validToDate" -> "2024-04-25",
        "quarantine" -> true
      )

      val expectedResult = EtmpExclusion(
        exclusionReason = "1",
        effectiveDate = "2024-02-25",
        validToDate = "2024-04-25",
        quarantine = true
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EtmpExclusion] mustBe JsSuccess(expectedResult)
    }
  }
}
