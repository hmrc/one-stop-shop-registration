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

package models.amend

import models.{Enumerable, WithName}

sealed trait EtmpSelfExclusionReason

object EtmpSelfExclusionReason extends Enumerable.Implicits {

  case object Reversal extends WithName("-1") with EtmpSelfExclusionReason

  case object NoLongerSupplies extends WithName("1") with EtmpSelfExclusionReason

  case object VoluntarilyLeaves extends WithName("5") with EtmpSelfExclusionReason

  case object TransferringMSID extends WithName("6") with EtmpSelfExclusionReason

  val values: Seq[EtmpSelfExclusionReason] = Seq(
    Reversal,
    NoLongerSupplies,
    VoluntarilyLeaves,
    TransferringMSID
  )

  implicit val enumerable: Enumerable[EtmpSelfExclusionReason] =
    Enumerable(values.map(v => v.toString -> v): _*)
}