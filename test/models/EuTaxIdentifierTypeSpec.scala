package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class EuTaxIdentifierTypeSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks {

  "must serialise and deserialise to and from all valid values" in {

    EuTaxIdentifierType.values.foreach {
      value =>
        val json = Json.toJson(value)
        json.validate[EuTaxIdentifierType] mustEqual JsSuccess(value)
    }
  }

  "must not deserialise from any invalid values" in {

    forAll(arbitrary[String]) {
      value =>

        whenever (!EuTaxIdentifierType.values.map(_.toString).contains(value)) {
          JsString(value).validate[EuTaxIdentifierType] mustEqual JsError("error.invalid")
        }
    }
  }
}
