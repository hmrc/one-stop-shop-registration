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

package models.enrolments

import com.typesafe.config.Config
import play.api.libs.json.{Json, OFormat}
import play.api.ConfigLoader
import uk.gov.hmrc.domain.Vrn

case class HistoricTraderForEnrolment(vrn: Vrn, groupId: String, userId: String)

object HistoricTraderForEnrolment {

  implicit val format: OFormat[HistoricTraderForEnrolment] = Json.format[HistoricTraderForEnrolment]

  implicit val seqHistoricTraderForEnrolment: ConfigLoader[Seq[HistoricTraderForEnrolment]] = new ConfigLoader[Seq[HistoricTraderForEnrolment]] {
    override def load(rootConfig: Config, path: String): Seq[HistoricTraderForEnrolment] = {
      import scala.jdk.CollectionConverters._

      val config = rootConfig.getConfig(path)

      rootConfig.getObject(path).keySet().asScala.map { key =>
        val value = config.getConfig(key)

        HistoricTraderForEnrolment(
          Vrn(value.getString("vrn")),
          value.getString("groupId"),
          value.getString("userId")
        )
      }.toSeq
    }
  }

}