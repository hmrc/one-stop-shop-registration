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

package uk.gov.hmrc.onestopshopregistration.repositories

import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.onestopshopregistration.models.InsertResult.{AlreadyExists, InsertSucceeded}
import uk.gov.hmrc.onestopshopregistration.models.{InsertResult, Registration}
import uk.gov.hmrc.onestopshopregistration.repositories.MongoErrors.Duplicate

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationRepository @Inject()(mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[Registration] (
    collectionName = "registrations",
    mongoComponent = mongoComponent,
    domainFormat   = Registration.format,
    indexes        = Seq(
      IndexModel(
        Indexes.ascending("vrn"),
        IndexOptions()
          .name("vrnIndex")
          .unique(true)
      )
    )
  ) {

  import uk.gov.hmrc.mongo.play.json.Codecs.toBson

  def insert(registration: Registration): Future[InsertResult] = {
    collection
      .insertOne(registration)
      .toFuture
      .map(_ => InsertSucceeded)
      .recover {
        case Duplicate(_) => AlreadyExists
      }
  }

  def get(vrn: Vrn): Future[Option[Registration]] = {
    collection.find(Filters.equal("vrn", toBson(vrn))).headOption
  }
}

