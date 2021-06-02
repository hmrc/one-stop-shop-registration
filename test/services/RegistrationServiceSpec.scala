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

import akka.http.scaladsl.util.FastFuture.successful
import base.BaseSpec
import models.InsertResult.InsertSucceeded
import models.Registration
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import repositories.RegistrationRepository

import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationServiceSpec extends BaseSpec {

  private val registration           = mock[Registration]
  private val registrationRepository = mock[RegistrationRepository]

  private val service = new RegistrationService(registrationRepository)

  private final val emulatedFailure = new RuntimeException("Emulated failure.")

  "insert()" - {

    "must return the result provided by the repository" in {

      when(registrationRepository.insert(any())).thenReturn(successful(InsertSucceeded))

      service.insert(registration).futureValue mustEqual InsertSucceeded
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
