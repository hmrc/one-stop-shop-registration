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

import config.AppConfig
import crypto.RegistrationEncrypter
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes, ReplaceOptions}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import models.InsertResult.{AlreadyExists, InsertSucceeded}
import models.{EncryptedRegistration, InsertResult, Registration}
import repositories.MongoErrors.Duplicate

import logging.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationRepository @Inject()(
                                        mongoComponent: MongoComponent,
                                        encrypter: RegistrationEncrypter,
                                        appConfig: AppConfig
                                      )(implicit ec: ExecutionContext)
  extends PlayMongoRepository[EncryptedRegistration] (
    collectionName = "registrations",
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

  import uk.gov.hmrc.mongo.play.json.Codecs.toBson

  private val encryptionKey = appConfig.encryptionKey

  def insert(registration: Registration): Future[InsertResult] = {
    val encryptedRegistration = encrypter.encryptRegistration(registration, registration.vrn, encryptionKey)

    collection
      .insertOne(encryptedRegistration)
      .toFuture
      .map(_ => InsertSucceeded)
      .recover {
        case Duplicate(_) => AlreadyExists
      }
  }

  def get(vrn: Vrn): Future[Option[Registration]] = {
    collection
      .find(Filters.equal("vrn", toBson(vrn)))
      .headOption
      .map(_.map {
        r =>
          encrypter.decryptRegistration(r, r.vrn, encryptionKey)
      })
  }

  def get(count: Int): Future[Seq[Registration]] = {
    collection
      .find
      .limit(count)
      .map(r => encrypter.decryptRegistration(r, r.vrn, encryptionKey))
      .toFuture
  }

  def updateDateOfFirstSale(registration: Registration): Future[Boolean] = {

    logger.info("About to update registration with dateOfFirstSale for vrn: " + obfuscateVrn(registration.vrn))

    val updatedRegistration = registration copy (dateOfFirstSale = Some(registration.commencementDate))

    logger.info("New registration object created with dateOfFirstSale for vrn: " + obfuscateVrn(registration.vrn))

    val encryptedRegistration = encrypter.encryptRegistration(updatedRegistration, updatedRegistration.vrn, encryptionKey)

    logger.info("Newly created registration object encrypted for vrn " + obfuscateVrn(registration.vrn))

    collection
      .replaceOne(
        filter = Filters.equal("vrn", updatedRegistration.vrn.vrn),
        replacement = encryptedRegistration,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => true)
  }

  private def obfuscateVrn(vrn: Vrn): String = vrn.vrn.take(5) + "****"
}

