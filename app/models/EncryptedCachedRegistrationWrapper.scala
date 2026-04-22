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

import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

final case class EncryptedCachedRegistrationWrapper(
                                      userId: String,
                                      vrn: Vrn,
                                      data: Option[String],
                                      lastUpdated: Instant
                                    )

object EncryptedCachedRegistrationWrapper {

  val reads: Reads[EncryptedCachedRegistrationWrapper] =
    (
      (__ \ "_id").read[String] and
      (__ \ "vrn").read[Vrn] and
      (__ \ "data").readNullable[String] and
      (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    ) (EncryptedCachedRegistrationWrapper.apply _)

  val writes: OWrites[EncryptedCachedRegistrationWrapper] =
    (
      (__ \ "_id").write[String] and
      (__ \ "vrn").write[Vrn] and
      (__ \ "data").writeNullable[String] and
      (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
    ) (registrationWrapper => Tuple.fromProductTyped(registrationWrapper))

  implicit val format: OFormat[EncryptedCachedRegistrationWrapper] = OFormat(reads, writes)
}
