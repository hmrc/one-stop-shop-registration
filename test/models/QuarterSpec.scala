package models

import base.BaseSpec
import models.Quarter._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.TryValues
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.util.Failure

class QuarterSpec extends BaseSpec with ScalaCheckPropertyChecks with TryValues {

  ".fromString" - {

    "must resolve for valid quarters" in {

      Quarter.fromString("Q1").success.value mustEqual Q1
      Quarter.fromString("Q2").success.value mustEqual Q2
      Quarter.fromString("Q3").success.value mustEqual Q3
      Quarter.fromString("Q4").success.value mustEqual Q4
    }

    "must not resolve for invalid quarters" in {

      forAll(arbitrary[String]) {
        string =>

          whenever(!Quarter.values.map(_.toString).contains(string)) {

            Quarter.fromString(string) mustBe a[Failure[_]]
          }
      }
    }
  }
}
