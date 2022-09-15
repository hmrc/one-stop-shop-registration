/*
 * Copyright 2022 HM Revenue & Customs
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

package services

import config.AppConfig
import connectors.RegistrationConnector
import connectors.RegistrationHttpParser.ValidateRegistrationResponse
import models.requests.RegistrationRequest
import models.{InsertResult, Registration}
import repositories.RegistrationRepository
import services.exclusions.ExclusionService
import uk.gov.hmrc.domain.Vrn

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationServiceRepositoryImpl @Inject()(
                                     registrationRepository: RegistrationRepository,
                                     registrationConnector: RegistrationConnector,
                                     clock: Clock,
                                     appConfig: AppConfig,
                                     exclusionService: ExclusionService
                                   )(implicit ec: ExecutionContext) extends RegistrationService {

  def createRegistration(request: RegistrationRequest): Future[InsertResult] =
    registrationRepository.insert(buildRegistration(request, clock))

  def get(vrn: Vrn): Future[Option[Registration]] = {
    for {
      maybeRegistration <- registrationRepository.get(vrn)
      maybeExcludedTrader <- exclusionService.findExcludedTrader(vrn)
    } yield {
      maybeRegistration.map { registration =>
        if (appConfig.exclusionsEnabled) {
          registration.copy(excludedTrader = maybeExcludedTrader)
        } else {
          registration
        }
      }
    }
  }

  override def validate(vrn: Vrn): Future[ValidateRegistrationResponse] = {
    registrationConnector.validateRegistration(vrn)
  }
}
