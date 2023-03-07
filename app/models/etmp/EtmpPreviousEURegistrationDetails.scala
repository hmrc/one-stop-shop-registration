/*
 * Copyright 2023 HM Revenue & Customs
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

sealed trait SchemeType

object SchemeType extends Enumerable.Implicits {
  case object OSSUnion extends WithName("OSS Union") with SchemeType
  case object OSSNonUnion extends WithName("OSS Non-Union") with SchemeType
  case object IOSSWithoutIntermediary extends WithName("IOSS without intermediary") with SchemeType
  case object IOSSWithIntermediary extends WithName("IOSS with intermediary") with SchemeType

  val values: Seq[SchemeType] = Seq(
    OSSUnion, OSSNonUnion, IOSSWithoutIntermediary, IOSSWithIntermediary
  )

  implicit val enumerable: Enumerable[SchemeType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}

case class EtmpPreviousEURegistrationDetails(issuedBy: String, registrationNumber: String, schemeType: SchemeType, intermediaryNumber: Option[String])

object EtmpPreviousEURegistrationDetails {

  implicit val format: OFormat[EtmpPreviousEURegistrationDetails] = Json.format[EtmpPreviousEURegistrationDetails]
}
