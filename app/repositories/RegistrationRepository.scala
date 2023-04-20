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

package repositories

import config.AppConfig
import crypto.RegistrationEncrypter
import org.mongodb.scala.model.{Filters, Indexes, IndexModel, IndexOptions, ReplaceOptions}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import models.repository.InsertResult.{AlreadyExists, InsertSucceeded}
import models.{EncryptedRegistration, Registration}
import repositories.MongoErrors.Duplicate
import logging.Logging
import models.repository.AmendResult.AmendSucceeded
import models.repository.{AmendResult, InsertResult}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationRepository @Inject()(
                                        mongoComponent: MongoComponent,
                                        encrypter: RegistrationEncrypter,
                                        appConfig: AppConfig
                                      )(implicit ec: ExecutionContext)
  extends PlayMongoRepository[EncryptedRegistration](
    collectionName = "registrations",
    mongoComponent = mongoComponent,
    domainFormat = EncryptedRegistration.format,
    replaceIndexes = true,
    indexes = Seq(
      IndexModel(
        Indexes.ascending("vrn"),
        IndexOptions()
          .name("vrnIndex")
          .unique(true)
      )
    )
  ) with Logging {

  import uk.gov.hmrc.mongo.play.json.Codecs.toBson

  private val encryptionKey = appConfig.encryptionKey

  def insert(registration: Registration): Future[InsertResult] = {
    val encryptedRegistration = encrypter.encryptRegistration(registration, registration.vrn, encryptionKey)

    collection
      .insertOne(encryptedRegistration)
      .toFuture()
      .map(_ => InsertSucceeded)
      .recover {
        case Duplicate(_) => AlreadyExists
      }
  }

  def get(vrn: Vrn): Future[Option[Registration]] = {
    collection
      .find(Filters.equal("vrn", toBson(vrn)))
      .headOption()
      .map(_.map {
        r =>
          encrypter.decryptRegistration(r, r.vrn, encryptionKey)
      })
  }


  def insertMany(registrations: List[Registration]): Future[InsertResult] = {
    val encryptedRegistrations = registrations.map(
      registration => encrypter.encryptRegistration(registration, registration.vrn, encryptionKey))

    collection
      .insertMany(encryptedRegistrations)
      .toFuture()
      .map(_ => InsertSucceeded)
      .recover {
        case Duplicate(_) => AlreadyExists
      }
  }

  def set(registration: Registration): Future[AmendResult] = {
    val encryptedRegistration = encrypter.encryptRegistration(registration, registration.vrn, encryptionKey)

    collection
      .replaceOne(
        filter = Filters.equal("vrn", toBson(registration.vrn)),
        replacement = encryptedRegistration,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => AmendSucceeded) // TODO check what happens when a registration doesn't exist
  }
}

