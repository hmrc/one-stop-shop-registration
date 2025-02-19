package models.binders

import base.BaseSpec
import play.api.mvc.PathBindable

import java.time.LocalDate

class BindersSpec extends BaseSpec {

  "PathBindable for LocalDate" - {

    val binder: PathBindable[LocalDate] = Binders.pathBindable

    "successfully bind a valid date string" in {
      val result = binder.bind("date", "2024-02-17")
      result mustBe Right(LocalDate.of(2024, 2, 17))
    }

    "fail to bind an invalid date string" in {
      val result = binder.bind("date", "invalid-date")
      result mustBe Left("Invalid date")
    }

    "successfully unbind a LocalDate" in {
      val date = LocalDate.of(2024, 2, 17)
      val result = binder.unbind("date", date)
      result mustBe "2024-02-17"
    }
  }
}
