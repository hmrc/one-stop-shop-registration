/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import play.api.libs.json.{Format, Json, OFormat, Reads, Writes}

import java.time.{Clock, LocalDate}
import scala.util.Try
import scala.util.matching.Regex

trait Period {
  val year: Int
  val quarter: Quarter
  val firstDay: LocalDate
  val lastDay: LocalDate
  val isPartial: Boolean

  val paymentDeadline: LocalDate = LocalDate.of(year, quarter.startMonth, 1).plusMonths(4).minusDays(1)

  def isOverdue(clock: Clock): Boolean = {
    paymentDeadline.isBefore(LocalDate.now(clock))
  }

}


case class StandardPeriod(year: Int, quarter: Quarter) extends Period {

  override val firstDay: LocalDate = LocalDate.of(year, quarter.startMonth, 1)
  override val lastDay: LocalDate = firstDay.plusMonths(3).minusDays(1)

  override val isPartial: Boolean = false

  override def toString: String = s"$year-${quarter.toString}"
}

object StandardPeriod {
  implicit val format: OFormat[StandardPeriod] = Json.format[StandardPeriod]
}

object Period {

  private val pattern: Regex = """(\d{4})-(Q[1-4])""".r.anchored

  def apply(yearString: String, quarterString: String): Try[Period] =
    for {
      year <- Try(yearString.toInt)
      quarter <- Quarter.fromString(quarterString)
    } yield StandardPeriod(year, quarter)

  def fromString(string: String): Option[Period] =
    string match {
      case pattern(yearString, quarterString) =>
        Period(yearString, quarterString).toOption
      case _ =>
        None
    }

  def reads: Reads[Period] =
    StandardPeriod.format.widen[Period] orElse
      PartialReturnPeriod.format.widen[Period]

  def writes: Writes[Period] = Writes {
    case s: StandardPeriod => Json.toJson(s)(StandardPeriod.format)
    case p: PartialReturnPeriod => Json.toJson(p)(PartialReturnPeriod.format)
  }

  implicit def format: Format[Period] = Format(reads, writes)

}