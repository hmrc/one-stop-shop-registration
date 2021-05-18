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

import org.mongodb.scala.model.ReplaceOptions
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.onestopshopregistration.models.Registration

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationRepository @Inject()(mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[Registration] (
    collectionName = "registrations",
    mongoComponent = mongoComponent,
    domainFormat   = Registration.format,
    indexes        = Seq.empty
  ) {

  def insert(registration: Registration): Future[Boolean] = {
    //    val updatedAnswers = registration copy()
    //
    //    collection
    //      .replaceOne(
    //        filter      = byId(updatedAnswers.id),
    //        replacement = updatedAnswers,
    //        options     = ReplaceOptions().upsert(true)
    //      )
    //      .toFuture
    //      .map(_ => true)
    //  }
Future.successful(true)
  }
}

