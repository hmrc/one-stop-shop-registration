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
import connectors.{EnrolmentsConnector, RegistrationConnector}
import models.InsertResult.InsertSucceeded
import models.RegistrationValidationResult
import models.requests.RegistrationRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers.running
import repositories.RegistrationRepository
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class RegistrationServiceRepositoryImplSpec extends BaseSpec with BeforeAndAfterEach {

  private val registrationRequest    = mock[RegistrationRequest]
  private val registrationRepository = mock[RegistrationRepository]
  private val registrationConnector = mock[RegistrationConnector]
  private val enrolmentsConnector = mock[EnrolmentsConnector]
  private val service = new RegistrationServiceRepositoryImpl(registrationRepository, registrationConnector, stubClock)

  private final val emulatedFailure = new RuntimeException("Emulated failure.")

  override def beforeEach(): Unit = {
    reset(registrationRepository, registrationConnector)
    super.beforeEach()
  }

  "RegistrationServiceRepositoryImpl is bound if the sendRegToEtmp toggle is false" in {
    val app =
      applicationBuilder
        .configure(
          "features.sendRegToEtmp" -> "false"
        )
        .build()

    running(app) {
      val service = app.injector.instanceOf[RegistrationService]
      service.getClass mustBe classOf[RegistrationServiceRepositoryImpl]
    }
  }

  ".createRegistration" - {

    "must create a registration from the request, save it and return the result of the save operation" in {

      when(registrationRepository.insert(any())).thenReturn(successful(InsertSucceeded))

      service.createRegistration(registrationRequest).futureValue mustEqual InsertSucceeded
    }

    "propagate any error" in {
      when(registrationRepository.insert(any())).thenThrow(emulatedFailure)

      val caught = intercept[RuntimeException] {
        service.createRegistration(registrationRequest).futureValue
      }

      caught mustBe emulatedFailure
    }
  }

  ".get" - {

    "must call registrationRepository.get" in {
      when(registrationRepository.get(any())) thenReturn Future.successful(None)
      service.get(Vrn("123456789")).futureValue
      verify(registrationRepository, times(1)).get(Vrn("123456789"))
    }
  }

  ".validate" - {
    "must make a call to the validate method in RegistrationConnector" in {
      when(registrationConnector.validateRegistration(any())) thenReturn Future.successful(Right(RegistrationValidationResult(true)))
      service.validate(vrn).futureValue
      verify(registrationConnector, times(1)).validateRegistration(vrn)
    }
  }
}
