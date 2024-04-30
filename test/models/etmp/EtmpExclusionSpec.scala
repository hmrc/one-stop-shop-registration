package models.etmp

import base.BaseSpec
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

import java.time.LocalDate

class EtmpExclusionSpec extends BaseSpec with ScalaCheckPropertyChecks {

  private val genEtmpExclusionReason: EtmpExclusionReason = Gen.oneOf(EtmpExclusionReason.values).sample.value

  "EtmpExclusion" - {

    "must deserialise/serialise to and from EtmpExclusion" in {

      val json = Json.obj(
        "exclusionReason" -> genEtmpExclusionReason,
        "effectiveDate" -> LocalDate.of(2023, 2, 1),
        "decisionDate" -> LocalDate.of(2023, 1, 1),
        "quarantine" -> true
      )

      val expectedResult = EtmpExclusion(
        exclusionReason = genEtmpExclusionReason,
        effectiveDate = LocalDate.of(2023, 2, 1),
        decisionDate = LocalDate.of(2023, 1, 1),
        quarantine = true
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EtmpExclusion] mustBe JsSuccess(expectedResult)
    }
  }

  "EtmpExclusionReason" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(EtmpExclusionReason.values)

      forAll(gen) {
        etmpExclusionReason =>

          JsString(etmpExclusionReason.toString).validate[EtmpExclusionReason].asOpt.value mustBe etmpExclusionReason
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!EtmpExclusionReason.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[EtmpExclusionReason] mustBe JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(EtmpExclusionReason.values)

      forAll(gen) {
        etmpExclusionReason =>

          Json.toJson(etmpExclusionReason) mustBe JsString(etmpExclusionReason.toString)
      }
    }
  }
}
