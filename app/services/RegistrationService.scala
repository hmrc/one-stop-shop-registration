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

package services

import models.requests.RegistrationRequest
import models.{InsertResult, Registration}
import repositories.RegistrationRepository
import uk.gov.hmrc.domain.Vrn

import java.time.{Clock, Instant}
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class RegistrationService @Inject() (
                                      registrationRepository: RegistrationRepository,
                                      clock: Clock
                                    ) {

  def createRegistration(request: RegistrationRequest): Future[InsertResult] =
    registrationRepository.insert(buildRegistration(request))

  private def buildRegistration(request: RegistrationRequest): Registration =
    Registration(
      vrn                          = request.vrn,
      registeredCompanyName        = request.registeredCompanyName,
      tradingNames                 = request.tradingNames,
      vatDetails                   = request.vatDetails,
      euRegistrations              = request.euRegistrations,
      contactDetails               = request.contactDetails,
      websites                     = request.websites,
      startDate                    = request.startDate,
      currentCountryOfRegistration = request.currentCountryOfRegistration,
      previousRegistrations        = request.previousRegistrations,
      bankDetails                  = request.bankDetails,
      submissionReceived           = Instant.now(clock)
    )

  def get(vrn: Vrn): Future[Option[Registration]] =
    registrationRepository.get(vrn)
}
