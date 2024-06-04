package models.etmp

import base.BaseSpec
import models.exclusions.ExclusionReason
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class EtmpExclusionSpec extends BaseSpec with ScalaCheckPropertyChecks {

  private val genExclusionReason: ExclusionReason = Gen.oneOf(ExclusionReason.values).sample.value

  "EtmpExclusion" - {

    "must deserialise/serialise to and from EtmpExclusion" in {

      val json = Json.obj(
        "exclusionReason" -> genExclusionReason,
        "effectiveDate" -> LocalDate.of(2023, 2, 1),
        "decisionDate" -> LocalDate.of(2023, 1, 1),
        "quarantine" -> true
      )

      val expectedResult = EtmpExclusion(
        exclusionReason = genExclusionReason,
        effectiveDate = LocalDate.of(2023, 2, 1),
        decisionDate = LocalDate.of(2023, 1, 1),
        quarantine = true
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EtmpExclusion] mustBe JsSuccess(expectedResult)
    }
  }
}
