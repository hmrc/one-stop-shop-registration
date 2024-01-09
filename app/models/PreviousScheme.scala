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

sealed trait PreviousScheme

object PreviousScheme extends Enumerable.Implicits {

  case object OSSU extends WithName("ossu") with PreviousScheme
  case object OSSNU extends WithName("ossnu") with PreviousScheme
  case object IOSSWOI extends WithName("iosswoi") with PreviousScheme
  case object IOSSWI extends WithName("iosswi") with PreviousScheme

  val values: Seq[PreviousScheme] = Seq(
    OSSU, OSSNU, IOSSWOI, IOSSWI
  )

  val iossValues: Seq[PreviousScheme] = Seq(
    IOSSWOI, IOSSWI
  )

  implicit val enumerable: Enumerable[PreviousScheme] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
