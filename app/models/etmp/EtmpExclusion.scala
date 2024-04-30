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

package models.etmp

import models.{Enumerable, WithName}
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class EtmpExclusion(
                          exclusionReason: EtmpExclusionReason,
                          effectiveDate: LocalDate,
                          decisionDate: LocalDate,
                          quarantine: Boolean
                        )

object EtmpExclusion {

  implicit val format: OFormat[EtmpExclusion] = Json.format[EtmpExclusion]
}

sealed trait EtmpExclusionReason

object EtmpExclusionReason extends Enumerable.Implicits {

  case object Reversal extends WithName("-1") with EtmpExclusionReason

  case object NoLongerSupplies extends WithName("1") with EtmpExclusionReason

  case object CeasedTrade extends WithName("2") with EtmpExclusionReason

  case object NoLongerMeetsConditions extends WithName("3") with EtmpExclusionReason

  case object FailsToComply extends WithName("4") with EtmpExclusionReason

  case object VoluntarilyLeaves extends WithName("5") with EtmpExclusionReason

  case object TransferringMSID extends WithName("6") with EtmpExclusionReason

  val values: Seq[EtmpExclusionReason] = Seq(
    Reversal,
    NoLongerSupplies,
    CeasedTrade,
    NoLongerMeetsConditions,
    FailsToComply,
    VoluntarilyLeaves,
    TransferringMSID
  )

  implicit val enumerable: Enumerable[EtmpExclusionReason] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
