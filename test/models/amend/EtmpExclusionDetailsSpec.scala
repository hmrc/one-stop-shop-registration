package models.amend

import base.BaseSpec
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

import java.time.LocalDate


class EtmpExclusionDetailsSpec extends BaseSpec  {

  "EtmpExclusionDetails" - {

    "must serialise/deserialise to and from EtmpExclusionDetails" in {

      val exclusionDetails = EtmpExclusionDetails(
        exclusionRequestDate = LocalDate.of(2023, 1, 1),
        exclusionReason = EtmpSelfExclusionReason.NoLongerSupplies,
        movePOBDate = Some(LocalDate.of(2024, 1, 1)),
        issuedBy = Some("DE"),
        vatNumber = Some("DE123")
      )

      val expectedJson = Json.obj(
        "exclusionReason" -> "1",
        "partyType" -> "NETP",
        "exclusionRequestDate" -> "2023-01-01",
        "vatNumber" -> "DE123",
        "movePOBDate" -> "2024-01-01",
        "issuedBy" -> "DE"
      )

      Json.toJson(exclusionDetails) mustBe expectedJson
      expectedJson.validate[EtmpExclusionDetails] mustBe JsSuccess(exclusionDetails)
    }

    "when all optional values are absent" in {

      val exclusionDetails = EtmpExclusionDetails(
        exclusionRequestDate = LocalDate.of(2023, 1, 1),
        exclusionReason = EtmpSelfExclusionReason.NoLongerSupplies,
        movePOBDate = None,
        issuedBy = None,
        vatNumber = None
      )

      val expectedJson = Json.obj(
        "exclusionReason" -> "1",
        "partyType" -> "NETP",
        "exclusionRequestDate" -> "2023-01-01",
      )

      Json.toJson(exclusionDetails) mustBe expectedJson
      expectedJson.validate[EtmpExclusionDetails] mustBe JsSuccess(exclusionDetails)
    }

    "when values are absent" in {

      val expectedJson = Json.obj()

      expectedJson.validate[EtmpExclusionDetails] mustBe a[JsError]
    }

    "when invalid values" in {

      val expectedJson = Json.obj(
        "exclusionReason" -> 12345,
        "partyType" -> "NETP",
        "exclusionRequestDate" -> "2023-01-01",
        "vatNumber" -> "DE123",
        "movePOBDate" -> "2024-01-01",
        "issuedBy" -> "DE"
      )

      expectedJson.validate[EtmpExclusionDetails] mustBe a[JsError]
    }

    "when null values" in {

      val expectedJson = Json.obj(
        "exclusionReason" -> JsNull,
        "partyType" -> "NETP",
        "exclusionRequestDate" -> "2023-01-01",
        "vatNumber" -> "DE123",
        "movePOBDate" -> "2024-01-01",
        "issuedBy" -> "DE"
      )

      expectedJson.validate[EtmpExclusionDetails] mustBe a[JsError]
    }
  }
}
