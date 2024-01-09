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

import crypto.EncryptedValue
import models.domain.{EncryptedVatCustomerInfo, VatCustomerInfo}
import play.api.libs.json.{JsValue, Json, OFormat, OWrites, Reads, __}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class SavedUserAnswers(
                             vrn: Vrn,
                             data: JsValue,
                             vatInfo: Option[VatCustomerInfo],
                             lastUpdated: Instant
                           )

object SavedUserAnswers {

  implicit val format: OFormat[SavedUserAnswers] = Json.format[SavedUserAnswers]
}

case class EncryptedSavedUserAnswers(
                                      vrn: Vrn,
                                      data: EncryptedValue,
                                      vatInfo: Option[EncryptedVatCustomerInfo],
                                      lastUpdated: Instant
                                    )

object EncryptedSavedUserAnswers {

  val reads: Reads[EncryptedSavedUserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "vrn").read[Vrn] and
        (__ \ "data").read[EncryptedValue] and
        (__ \ "vatInfo").readNullable[EncryptedVatCustomerInfo] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
      ) (EncryptedSavedUserAnswers.apply _)
  }

  val writes: OWrites[EncryptedSavedUserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "vrn").write[Vrn] and
        (__ \ "data").write[EncryptedValue] and
        (__ \ "vatInfo").writeNullable[EncryptedVatCustomerInfo] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
      ) (unlift(EncryptedSavedUserAnswers.unapply))
  }

  implicit val format: OFormat[EncryptedSavedUserAnswers] = OFormat(reads, writes)
}

