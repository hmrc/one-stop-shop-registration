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

package repositories

import config.AppConfig
import logging.Logging
import models.RegistrationStatus
import models.repository.InsertResult.{AlreadyExists, InsertSucceeded}
import models.repository.InsertResult
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes, ReplaceOptions}
import repositories.MongoErrors.Duplicate
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.MongoComponent

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class RegistrationStatusRepository @Inject()(
                                              mongoComponent: MongoComponent,
                                              appConfig: AppConfig
                                            )(implicit ec: ExecutionContext)
  extends PlayMongoRepository[RegistrationStatus] (
    collectionName = "registration-status",
    mongoComponent = mongoComponent,
    domainFormat   = RegistrationStatus.format,
    replaceIndexes = true,
    indexes        = Seq(
      IndexModel(
        Indexes.ascending("subscriptionId"),
        IndexOptions()
          .name("subscriptionIdIndex")
          .unique(true)
          .expireAfter(appConfig.registrationStatusTtl, TimeUnit.HOURS)

      )
    )
  ) with Logging {

  private def bySubscriptionId(subscriptionId: String): Bson = Filters.equal("subscriptionId", subscriptionId)

  def insert(registrationStatus: RegistrationStatus): Future[InsertResult] = {

    collection
      .insertOne(registrationStatus)
      .toFuture()
      .map(_ => InsertSucceeded)
      .recover {
        case Duplicate(_) => AlreadyExists
      }
  }

  def set(registrationStatus: RegistrationStatus): Future[RegistrationStatus] = {

    collection
      .replaceOne(
        filter = bySubscriptionId(registrationStatus.subscriptionId),
        replacement = registrationStatus,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => registrationStatus)
  }

  def get(subscriptionId: String): Future[Option[RegistrationStatus]] = {
    collection
      .find(bySubscriptionId(subscriptionId)).headOption()
  }

  def delete(subscriptionId: String): Future[Boolean] =
    collection
      .deleteOne(bySubscriptionId(subscriptionId))
      .toFuture()
      .map(_ => true)
}
