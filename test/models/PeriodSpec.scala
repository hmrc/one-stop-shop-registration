package models

import base.BaseSpec
import models.Period.getPeriod
import models.Quarter.Q1
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.LocalDate
import java.time.format.DateTimeFormatter


class PeriodSpec extends BaseSpec with ScalaCheckPropertyChecks {

  ".fromString" - {

    "must resolve for valid periods" in {

      Period.fromString("2021-Q1").get mustEqual StandardPeriod(2021, Q1)

    }

    "must return None for invalid periods" in {

      forAll(arbitrary[String]) {
        string =>

          Period.fromString(string) mustBe None
      }
    }
  }

  ".getPeriod" - {

    "must return the correct period when given a LocalDate" in {

      val date: LocalDate = LocalDate.now(stubClock)
      val quarter: Quarter = Quarter.fromString(date.format(DateTimeFormatter.ofPattern("QQQ"))).get

      val expectedResult: Period = StandardPeriod(date.getYear, quarter)

      getPeriod(date) mustBe expectedResult
    }
  }
}