package models.exclusions

import base.BaseSpec
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

import java.time.LocalDate

class ExcludedTraderSpec extends BaseSpec with ScalaCheckPropertyChecks {

  private val genExclusionReason: ExclusionReason = Gen.oneOf(ExclusionReason.values).sample.value
  private val genVrn = arbitraryVrn.arbitrary.sample.value
  private val quarantined: Boolean = arbitrary[Boolean].sample.value

  "ExcludedTrader" - {

    "must deserialise/serialise to and from ExcludedTrader" in {

      val json = Json.obj(
        "exclusionReason" -> genExclusionReason,
        "effectiveDate" -> LocalDate.of(2023, 2, 1),
        "vrn" -> genVrn,
        "quarantined" -> quarantined
      )

      val expectedResult = ExcludedTrader(
        exclusionReason = genExclusionReason,
        effectiveDate = LocalDate.of(2023, 2, 1),
        vrn = genVrn,
        quarantined = quarantined
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[ExcludedTrader] mustBe JsSuccess(expectedResult)
    }
  }

  "ExclusionReason" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(ExclusionReason.values)

      forAll(gen) {
        exclusionReason =>

          JsString(exclusionReason.toString).validate[ExclusionReason].asOpt.value mustBe exclusionReason
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ExclusionReason.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[ExclusionReason] mustBe JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(ExclusionReason.values)

      forAll(gen) {
        exclusionReason =>

          Json.toJson(exclusionReason) mustBe JsString(exclusionReason.toString)
      }
    }
  }
}
