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

package uk.gov.hmrc.onestopshopregistration.service

import uk.gov.hmrc.onestopshopregistration.models.Registration
import uk.gov.hmrc.onestopshopregistration.repositories.RegistrationRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationService @Inject() (
  registrationRepository: RegistrationRepository
)(implicit ec: ExecutionContext) {

  def insert(registration: Registration): Future[Boolean] = {
    registrationRepository.insert(registration)
  }

}
