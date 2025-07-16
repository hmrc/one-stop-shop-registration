package models

import base.BaseSpec
import models.Period.getPeriod
import models.Quarter._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Success


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

  ".apply" - {
    "must return a valid StandardPeriod for a valid year and quarter" in {
      val period = Period("2021", "Q1")
      period mustBe Success(StandardPeriod(2021, Q1))
    }
  }

  ".isOverdue" - {

    "must return true if the payment deadline is overdue" in {
      val period = StandardPeriod(2020, Q1)

      period.isOverdue(stubClock) mustBe true
    }

    "must return false if the payment deadline has not passed" in {
      val nextYear = LocalDate.now(stubClock).plusYears(1).getYear
      val period = StandardPeriod(nextYear, Q1)

      period.isOverdue(stubClock) mustBe false
    }
  }

  ".getNextPeriod" - {
    "must return the correct next period in the same year" in {
      val period = StandardPeriod(2023, Q2)

      period.getNextPeriod mustBe StandardPeriod(2023, Q3)
    }
    "must return the correct next period when the quarter is in the following year" in {
      val period = StandardPeriod(2023, Q4)

      period.getNextPeriod mustBe StandardPeriod(2024, Q1)
    }
  }

  ".getPreviousPeriod" - {
    "must return the correct previous period when the quarter is in the same year" in {
      val period = StandardPeriod(2023, Q3)

      period.getPreviousPeriod mustBe StandardPeriod(2023, Q2)
    }
    "must return the correct previous period when the qyart is in the previous year" in {
      val period = StandardPeriod(2023, Q1)

      period.getPreviousPeriod mustBe StandardPeriod(2022, Q4)
    }
  }
}

