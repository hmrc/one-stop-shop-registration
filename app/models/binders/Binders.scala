/*
 * Copyright 2022 HM Revenue & Customs
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

package models.binders

import play.api.Logging
import play.api.mvc.PathBindable

import java.time.{LocalDate}
import scala.util.{Failure, Success, Try}

object Binders extends Logging {

  implicit val pathBindable: PathBindable[LocalDate] = new PathBindable[LocalDate] {

    override def bind(key: String, value: String): Either[String, LocalDate] = {
      Try {
        LocalDate.parse(value, Format.dateTimeFormatter)
      } match {
        case Success(value) => Right(value)
        case Failure(_) => Left("Invalid date")
      }
    }

    override def unbind(key: String, value: LocalDate): String =
      Format.dateTimeFormatter.format(value)
  }

}
