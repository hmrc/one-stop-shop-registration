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

import akka.http.scaladsl.util.FastFuture.successful
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.onestopshopregistration.base.BaseSpec
import uk.gov.hmrc.onestopshopregistration.models.Registration
import uk.gov.hmrc.onestopshopregistration.repositories.RegistrationRepository
import utils.RegistrationData

import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationServiceSpec extends BaseSpec {

  private val registration           = mock[Registration]
  private val registrationRepository = mock[RegistrationRepository]

  private val service = new RegistrationService(registrationRepository)
  

  private final val emulatedFailure = new RuntimeException("Emulated failure.")

  "insert()" - {

    "return true after it is inserted in the database collection" in {
      when(registrationRepository.insert(any())).thenReturn(successful(true))

      service.insert(registration).futureValue mustEqual true
    }

    "propagate any error" in {
      when(registrationRepository.insert(any())).thenThrow(emulatedFailure)

      val caught = intercept[RuntimeException] {
        service.insert(registration).futureValue
      }

      caught mustBe emulatedFailure
    }
  }
}
