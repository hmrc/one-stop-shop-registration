package models.exclusions

import base.BaseSpec
import models.amend.EtmpSelfExclusionReason
import play.api.libs.json.*

import java.time.LocalDate

class ExclusionDetailsSpec extends BaseSpec {

  "ExclusionDetails" - {

    "must deserialise/serialise to and from ExclusionDetails" in {

      val json = Json.obj(
        "exclusionReason" -> "1",
        "exclusionRequestDate" -> "2023-01-01",
        "vatNumber" -> "DE123",
        "movePOBDate" -> "2024-01-01",
        "issuedBy" -> "DE"
      )

      val expectedResult = ExclusionDetails(
        exclusionRequestDate = LocalDate.of(2023, 1, 1),
        exclusionReason = EtmpSelfExclusionReason.NoLongerSupplies,
        movePOBDate = Some(LocalDate.of(2024, 1, 1)),
        issuedBy = Some("DE"),
        vatNumber = Some("DE123")
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[ExclusionDetails] mustBe JsSuccess(expectedResult)
    }

    "must fail to deserialise missing values" in {

      val json = Json.obj()

      json.validate[ExclusionDetails] mustBe a[JsError]
    }

    "must fail to deserialise invalid values" in {

      val json = Json.obj(
        "exclusionReason" -> "1",
        "exclusionRequestDate" -> "2023-01-01",
        "vatNumber" -> "DE123",
        "movePOBDate" -> "2024-01-01",
        "issuedBy" -> 12345
      )

      json.validate[ExclusionDetails] mustBe a[JsError]
    }

    "must fail to deserialise null values" in {

      val json = Json.obj(
        "exclusionReason" -> JsNull,
        "exclusionRequestDate" -> "2023-01-01",
        "vatNumber" -> "DE123",
        "movePOBDate" -> "2024-01-01",
        "issuedBy" -> "DE"
      )

      json.validate[ExclusionDetails] mustBe a[JsError]
    }
  }
}
