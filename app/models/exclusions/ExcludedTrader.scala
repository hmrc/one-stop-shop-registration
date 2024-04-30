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
import models.{Period, Quarter, StandardPeriod}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success}

case class ExcludedTrader(
                           vrn: Vrn,
                           exclusionReason: Int,
                           effectivePeriod: Period,
                           effectiveDate: Option[LocalDate]
                         )

object ExcludedTrader extends Logging {

  def fromEtmpExclusion(
                         vrn: Vrn,
                         etmpExclusion: EtmpExclusion
                       ): ExcludedTrader = {
    ExcludedTrader(
      vrn = vrn,
      exclusionReason = convertExclusionReason(etmpExclusion.exclusionReason),
      effectivePeriod = getPeriod(etmpExclusion.effectiveDate),
      effectiveDate = Some(etmpExclusion.effectiveDate)
    )
  }

  private def convertExclusionReason(exclusionReason: EtmpExclusionReason): Int = {
    exclusionReason match {
      case Reversal => -1
      case NoLongerSupplies => 1
      case CeasedTrade => 2
      case NoLongerMeetsConditions => 3
      case FailsToComply => 4
      case VoluntarilyLeaves => 5
      case TransferringMSID => 6
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



