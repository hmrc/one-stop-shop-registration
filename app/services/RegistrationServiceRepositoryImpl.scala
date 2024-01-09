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

package services

import config.AppConfig
import controllers.actions.AuthorisedMandatoryVrnRequest
import models.Registration
import models.repository.{AmendResult, InsertResult}
import models.requests.RegistrationRequest
import repositories.RegistrationRepository
import services.exclusions.ExclusionService
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationServiceRepositoryImpl @Inject()(
                                     registrationRepository: RegistrationRepository,
                                     clock: Clock,
                                     appConfig: AppConfig,
                                     exclusionService: ExclusionService
                                   )(implicit ec: ExecutionContext) extends RegistrationService {

  def createRegistration(registrationRequest: RegistrationRequest)
                        (implicit hc: HeaderCarrier, request: AuthorisedMandatoryVrnRequest[_]): Future[InsertResult] =
    registrationRepository.insert(buildRegistration(registrationRequest, clock))

  def get(vrn: Vrn)(implicit hc: HeaderCarrier, request: AuthorisedMandatoryVrnRequest[_]): Future[Option[Registration]] = {
    getRegistration(vrn)
  }

  def getWithoutAudit(vrn: Vrn)(implicit hc: HeaderCarrier): Future[Option[Registration]] = {
    getRegistration(vrn)
  }

  private def getRegistration(vrn: Vrn): Future[Option[Registration]] = {
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

  def amend(registrationRequest: RegistrationRequest)(implicit hc: HeaderCarrier, request: AuthorisedMandatoryVrnRequest[_]): Future[AmendResult] =
    registrationRepository.set(buildRegistration(registrationRequest, clock))

  def amendWithoutAudit(registrationRequest: RegistrationRequest)(implicit hc: HeaderCarrier): Future[AmendResult] =
    registrationRepository.set(buildRegistration(registrationRequest, clock))
}
