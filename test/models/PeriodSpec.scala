package models

import base.BaseSpec
import models.Quarter.Q1
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks


class PeriodSpec extends BaseSpec with ScalaCheckPropertyChecks {

  ".fromString" - {

    "must resolve for valid periods" in {

      Period.fromString("2021-Q1").get mustEqual Period(2021, Q1)

    }

    "must return None for invalid periods" in {

      forAll(arbitrary[String]) {
        string =>

        Period.fromString(string) mustBe None
      }
    }
  }

}
