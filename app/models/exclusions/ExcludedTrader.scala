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

package models.exclusions

import logging.Logging
import models.etmp.EtmpExclusionReason.{CeasedTrade, FailsToComply, NoLongerMeetsConditions, NoLongerSupplies, Reversal, TransferringMSID, VoluntarilyLeaves}
import models.etmp.{EtmpExclusion, EtmpExclusionReason}
import models.{Enumerable, Period, Quarter, StandardPeriod, WithName}
import models.exclusions.ExcludedTrader.getPeriod
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success}

case class ExcludedTrader(
                           vrn: Vrn,
                           exclusionReason: ExclusionReason,
                           effectiveDate: LocalDate
                         ) {

  val finalReturnPeriod: Period = {
    if(exclusionReason == ExclusionReason.TransferringMSID) {
      getPeriod(effectiveDate)
    } else {
      getPeriod(effectiveDate).getNextPeriod
    }
  }
}

object ExcludedTrader extends Logging {

  def fromEtmpExclusion(
                         vrn: Vrn,
                         etmpExclusion: EtmpExclusion
                       ): ExcludedTrader = {
    ExcludedTrader(
      vrn = vrn,
      exclusionReason = convertExclusionReason(etmpExclusion.exclusionReason),
      effectiveDate = etmpExclusion.effectiveDate
    )
  }

  private def convertExclusionReason(exclusionReason: EtmpExclusionReason): ExclusionReason = {
    exclusionReason match {
      case Reversal => ExclusionReason.Reversal
      case NoLongerSupplies => ExclusionReason.NoLongerSupplies
      case CeasedTrade => ExclusionReason.CeasedTrade
      case NoLongerMeetsConditions => ExclusionReason.NoLongerMeetsConditions
      case FailsToComply => ExclusionReason.FailsToComply
      case VoluntarilyLeaves => ExclusionReason.VoluntarilyLeaves
      case TransferringMSID => ExclusionReason.TransferringMSID
      case _ =>
        val message: String = "Invalid Exclusion Reason"
        logger.error(message)
        throw new IllegalStateException(message)
    }
  }

  private def getPeriod(date: LocalDate): Period = {
    val quarter = Quarter.fromString(date.format(DateTimeFormatter.ofPattern("QQQ")))

    quarter match {
      case Success(value) =>
        StandardPeriod(date.getYear, value)
      case Failure(exception) =>
        throw exception
    }
  }

  implicit val format: OFormat[ExcludedTrader] = Json.format[ExcludedTrader]
}

sealed trait ExclusionSource
object HMRC extends ExclusionSource
object TRADER extends ExclusionSource

sealed trait ExclusionReason {
  val exclusionSource: ExclusionSource
  val numberValue: Int
}

object ExclusionReason extends Enumerable.Implicits {

  case object Reversal extends WithName("-1")  with ExclusionReason {
    val exclusionSource: ExclusionSource = HMRC
    val numberValue: Int = -1
  }

  case object NoLongerSupplies extends WithName("1") with ExclusionReason {
    val exclusionSource: ExclusionSource = TRADER
    val numberValue: Int = 1
  }

  case object CeasedTrade extends WithName("2") with ExclusionReason {
    val exclusionSource: ExclusionSource = HMRC
    val numberValue: Int = 2
  }

  case object NoLongerMeetsConditions extends WithName("3") with ExclusionReason {
    val exclusionSource: ExclusionSource = TRADER
    val numberValue: Int = 3
  }

  case object FailsToComply extends WithName("4") with ExclusionReason {
    val exclusionSource: ExclusionSource = HMRC
    val numberValue: Int = 4
  }

  case object VoluntarilyLeaves extends WithName("5") with ExclusionReason {
    val exclusionSource: ExclusionSource = TRADER
    val numberValue: Int = 5
  }

  case object TransferringMSID extends WithName("6") with ExclusionReason {
    val exclusionSource: ExclusionSource = TRADER
    val numberValue: Int = 6
  }

  val values: Seq[ExclusionReason] = Seq(
    Reversal,
    NoLongerSupplies,
    CeasedTrade,
    NoLongerMeetsConditions,
    FailsToComply,
    VoluntarilyLeaves,
    TransferringMSID
  )

  implicit val enumerable: Enumerable[ExclusionReason] =
    Enumerable(values.map(v => v.toString -> v): _*)
}



