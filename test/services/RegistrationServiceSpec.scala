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
import config.AppConfig
import connectors.RegistrationConnector
import models.InsertResult.InsertSucceeded
import models.NotFound
import models.requests.RegistrationRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import repositories.RegistrationRepository
import testutils.RegistrationData.registration
import uk.gov.hmrc.domain.Vrn

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationServiceSpec extends BaseSpec with BeforeAndAfterEach {

  private val registrationRequest    = mock[RegistrationRequest]
  private val registrationRepository = mock[RegistrationRepository]
  private val registrationConnector = mock[RegistrationConnector]
  private val appConfig = mock[AppConfig]

  private val service = new RegistrationService(appConfig, registrationRepository, registrationConnector, stubClock)

  private final val emulatedFailure = new RuntimeException("Emulated failure.")

  override def beforeEach(): Unit = {
    reset(appConfig, registrationRepository, registrationConnector)
    super.beforeEach()
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

    "must call registrationRepository.get when sendRegToEtmp Flag is false" in {
      when(appConfig.sendRegToEtmp) thenReturn false
      when(registrationRepository.get(any())) thenReturn Future.successful(None)
      service.get(Vrn("123456789")).futureValue
      verify(registrationRepository, times(1)).get(Vrn("123456789"))
    }

    "must return a Some(registration) when the connector returns right and sendRegToEtmp Flag is true" in {
      when(appConfig.sendRegToEtmp) thenReturn true
      when(registrationConnector.get(any())) thenReturn Future.successful(Right(registration))
      service.get(Vrn("123456789")).futureValue mustBe Some(registration)
      verify(registrationConnector, times(1)).get(Vrn("123456789"))
    }

    "must return a None when the connector returns Left(error) and sendRegToEtmp Flag is true" in {
      when(appConfig.sendRegToEtmp) thenReturn true
      when(registrationConnector.get(any())) thenReturn Future.successful(Left(NotFound))
      service.get(Vrn("123456789")).futureValue mustBe None
      verify(registrationConnector, times(1)).get(Vrn("123456789"))
    }
  }
}
