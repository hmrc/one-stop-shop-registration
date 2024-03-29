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

package services.exclusions

import config.AppConfig
import logging.Logging
import models.exclusions.ExcludedTrader
import uk.gov.hmrc.domain.Vrn
import utils.HashingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class ExclusionService @Inject()(
                                  hashingUtil: HashingUtil,
                                  appConfig: AppConfig
                                ) extends Logging {

  def findExcludedTrader(vrn: Vrn): Future[Option[ExcludedTrader]] =
    Future.successful({
      appConfig.excludedTraders.find { e =>
        hashingUtil.verifyValue(vrn.vrn, e.hashedVrn)
      }.map { e =>
        ExcludedTrader(vrn, e.exclusionReason, e.effectivePeriod, e.effectiveDate)
      }
    }
    )

}