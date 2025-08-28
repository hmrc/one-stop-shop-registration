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

package controllers.cron

import config.AppConfig
import logging.Logging
import org.apache.pekko.actor.{ActorSystem, Cancellable}
import services.cron.CronService

import javax.inject.*
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*

@Singleton
class CronController @Inject()(
                                system: ActorSystem,
                                cronService: CronService,
                                appConfig: AppConfig,
                                initialDelay: FiniteDuration = 10.seconds,
                                interval: FiniteDuration = 5.hours
                              )(implicit ec: ExecutionContext) extends Logging {

  private var runCount: Int = 0

  private val cancellable: Cancellable =
    system.scheduler.scheduleAtFixedRate(
      initialDelay = initialDelay,
      interval = interval
    ) { () =>
      if (appConfig.lastUpdatedFeatureSwitch) {
        cronService.fixExpiryDates().map { entriesChanged =>
          runCount += 1
          logger.info(s"Implementing TTL: $entriesChanged documents were read as last updated now and set to current date & time.")

          if (runCount >= 2) {
            logger.info("The TTL updating job has run twice. Scheduler cancelled.")
            cancellable.cancel()
          }
        }
      } else {
        logger.info("ExpiryScheduler disabled; not starting.")
        cancellable.cancel()
      }

    }
}
