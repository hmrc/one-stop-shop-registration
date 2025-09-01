/*
 * Copyright 2025 HM Revenue & Customs
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

package services.cron

import config.AppConfig
import logging.Logging
import org.apache.pekko.actor.ActorSystem
import repositories.RegistrationStatusRepository

import javax.inject.*
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*

trait CronService

class CronServiceImpl @Inject()(
                                 system: ActorSystem,
                                 appConfig: AppConfig,
                                 registrationStatusRepository: RegistrationStatusRepository
                               )(implicit ec: ExecutionContext) extends Logging with CronService {

  system.scheduler.scheduleOnce(
    delay = appConfig.delay.microseconds
  ) {
    println(appConfig.lastUpdatedFeatureSwitch)
    if (appConfig.lastUpdatedFeatureSwitch) {
      registrationStatusRepository.fixAllDocuments().map { entriesChanged =>
        logger.info(s"Implementing TTL: ${entriesChanged.size} documents were read as last updated Instant.now and set to current date & time.")
      }
    } else {
      logger.info("ExpiryScheduler disabled; not starting.")
    }
  }
}