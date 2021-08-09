/*
 * Copyright 2021 HM Revenue & Customs
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

package repositories

import logging.Logging
import models.EncryptedRegistration
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationBackUpRepository @Inject()(
                                              mongoComponent: MongoComponent
                                            )(implicit ec: ExecutionContext)
  extends PlayMongoRepository[EncryptedRegistration] (
    collectionName = "registrations-backup",
    mongoComponent = mongoComponent,
    domainFormat   = EncryptedRegistration.format,
    replaceIndexes = true,
    indexes        = Seq(
      IndexModel(
        Indexes.ascending("vrn"),
        IndexOptions()
          .name("vrnIndex")
          .unique(true)
      )
    )
  ) with Logging {

  def insertMany(encryptedRegistrations: Seq[EncryptedRegistration]): Future[Boolean] = {
      collection
        .insertMany(encryptedRegistrations)
        .toFuture
        .map(_ => true)
        .recover {
          case ex =>
            logger.error(s"Failed to insert many Encrypted Registrations ${ex.getMessage}")
            false
      }
  }
}