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
import models.Registration
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.*
import play.api.libs.json.Format
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.domain.Vrn

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CachedRegistrationRepository @Inject()(
                                              mongoComponent: MongoComponent,
                                              appConfig: AppConfig,
                                              clock: Clock
                                      )(implicit ec: ExecutionContext)
  extends PlayMongoRepository[RegistrationWrapper](
    collectionName = "cachedRegistrations",
    mongoComponent = mongoComponent,
    domainFormat   = RegistrationWrapper.format,
    indexes        = Seq(
      IndexModel(
        Indexes.ascending("lastUpdated"),
        IndexOptions()
          .name("lastUpdatedIdx")
          .expireAfter(appConfig.registrationCacheTtl, TimeUnit.MINUTES)
      )
    )
  ) {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  private def byIdAndVrn(id: String, vrn: Vrn): Bson =
    Filters.and(
      Filters.equal("_id", id),
      Filters.equal("registration.vrn", vrn.vrn)
    )


  def get(userId: String, vrn: Vrn): Future[Option[RegistrationWrapper]] =
    collection
      .find(byIdAndVrn(userId, vrn))
      .headOption()

  def set(userId: String, vrn: Vrn, registration: Option[Registration]): Future[Boolean] = {

    val wrapper = RegistrationWrapper(userId, registration, Instant.now(clock))

    collection
      .replaceOne(
        filter      = byIdAndVrn(userId, vrn),
        replacement = wrapper,
        options     = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => true)
  }

  def clear(id: String, vrn: Vrn): Future[Boolean] = {

    collection
      .deleteOne(
        filter = byIdAndVrn(id, vrn)
      )
      .toFuture()
      .map(_ => true)

  }
}
