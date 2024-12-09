package models

import java.time.LocalDate
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, Json}

class PartialReturnPeriodSpec extends AnyWordSpec with Matchers {

    val firstDay = LocalDate.of(2024, 1, 1)
    val lastDay = LocalDate.of(2024, 3, 31)
    val quarter = Quarter.Q1
    val year = 2024

    val partialReturnPeriod = PartialReturnPeriod(firstDay, lastDay, year, quarter)

    "PartialReturnPeriod" should {
        "serialise and deserialise correctly" in {
            val expectedJson = Json.obj(
                "firstDay" -> "2024-01-01",
                "lastDay" -> "2024-03-31",
                "year" -> 2024,
                "quarter" -> "Q1"
            )

            val json = Json.toJson(partialReturnPeriod)
            json mustBe expectedJson
            json.validate[PartialReturnPeriod] mustEqual JsSuccess(partialReturnPeriod)
        }
    }
}