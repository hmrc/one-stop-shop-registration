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

package config

import models.enrolments.{HistoricTraderForEnrolment, TraderSubscriptionId}
import models.exclusions.HashedExcludedTrader
import play.api.Configuration

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(config: Configuration) {

  val appName: String = config.get[String]("appName")

  val encryptionKey: String = config.get[String]("mongodb.encryption.key")
  val duplicateRegistrationIntoRepository: Boolean = config.get[Boolean]("features.duplicateRegistrationIntoRepository")
  val cacheTtl: Int = config.get[Int]("mongodb.timeToLiveInDays")
  val registrationStatusTtl: Int = config.get[Int]("mongodb.timeToLiveInHours")

  val exclusionsEnabled: Boolean = config.get[Boolean]("features.exclusions.enabled")
  val exclusionsHashingKey: String = config.get[String]("features.exclusions.hashing-key")
  val excludedTraders: Seq[HashedExcludedTrader] = config.get[Seq[HashedExcludedTrader]]("features.exclusions.excluded-traders")
  val maxRetryCount: Int = config.get[Int]("features.maxRetryCount")
  val delay: Int = config.get[Int]("features.delay")

  val externalEntryTtlDays: Int = config.get[Int]("features.externalEntry.ttlInDays")
  val externalEntryJourneyStartReturnUrl: String = config.get[String]("features.externalEntry.urls.journeyStart")
  val externalEntryNoMoreWelshReturnUrl: String = config.get[String]("features.externalEntry.urls.noMoreWelshJourneyStart")

  val subscriptionIds: Seq[TraderSubscriptionId] = config.get[Seq[TraderSubscriptionId]]("features.fallbackEnrolment.traders")

  val historicTradersForEnrolmentEnabled: Boolean = config.get[Boolean]("features.enroll-historic-registration.enabled")
  val historicTradersForEnrolment: Seq[HistoricTraderForEnrolment] = config.get[Seq[HistoricTraderForEnrolment]]("features.enroll-historic-registration.historic-traders")
}
