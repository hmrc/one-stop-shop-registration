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
import controllers.actions.AuthorisedMandatoryVrnRequest
import models.exclusions.ExcludedTrader
import models.repository.AmendResult.AmendSucceeded
import models.repository.InsertResult.InsertSucceeded
import models.requests.RegistrationRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import repositories.RegistrationRepository
import services.exclusions.ExclusionService
import testutils.RegistrationData.registration
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationServiceRepositoryImplSpec extends BaseSpec with BeforeAndAfterEach {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private val registrationRequest = mock[RegistrationRequest]
  private val registrationRepository = mock[RegistrationRepository]
  private val mockConfig = mock[AppConfig]
  private val exclusionService = mock[ExclusionService]
  private val registrationService = new RegistrationServiceRepositoryImpl(registrationRepository, stubClock, mockConfig, exclusionService)

  implicit private lazy val ar: AuthorisedMandatoryVrnRequest[AnyContent] = AuthorisedMandatoryVrnRequest(FakeRequest(), userId, vrn)

  private final val emulatedFailure = new RuntimeException("Emulated failure.")

  override def beforeEach(): Unit = {
    reset(registrationRepository)
    reset(exclusionService)
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

      when(registrationRequest.submissionReceived) thenReturn None
      when(registrationRepository.insert(any())).thenReturn(successful(InsertSucceeded))

      registrationService.createRegistration(registrationRequest).futureValue mustEqual InsertSucceeded
    }

    "propagate any error" in {
      when(registrationRequest.submissionReceived) thenReturn None
      when(registrationRepository.insert(any())).thenThrow(emulatedFailure)

      val caught = intercept[RuntimeException] {
        registrationService.createRegistration(registrationRequest).futureValue
      }

      caught mustBe emulatedFailure
    }
  }

  ".get" - {

    "must call registrationRepository.get" in {
      when(registrationRepository.get(any())) thenReturn Future.successful(None)
      when(exclusionService.findExcludedTrader(any())) thenReturn Future.successful(None)
      registrationService.get(Vrn("123456789")).futureValue
      verify(registrationRepository, times(1)).get(Vrn("123456789"))
    }

    "when exclusion is enabled and trader is excluded" - {

      val excludedTrader: ExcludedTrader = ExcludedTrader(vrn, 4, period, None)

      "must return a Some(registration) when the connector returns right" in {
        when(registrationRepository.get(any())) thenReturn Future.successful(Some(registration))
        when(mockConfig.exclusionsEnabled) thenReturn true
        when(exclusionService.findExcludedTrader(any())) thenReturn Future.successful(Some(excludedTrader))
        registrationService.get(Vrn("123456789")).futureValue mustBe Some(registration.copy(excludedTrader = Some(excludedTrader)))
        verify(registrationRepository, times(1)).get(Vrn("123456789"))
      }
    }

  }

  ".amendRegistration" - {

    "must amend a registration from the request, save it and return the result of the save operation" in {

      when(registrationRequest.submissionReceived) thenReturn Some(Instant.now())
      when(registrationRepository.set(any())).thenReturn(successful(AmendSucceeded))

      registrationService.amend(registrationRequest).futureValue mustEqual AmendSucceeded
    }

    "propagate any error" in {

      when(registrationRequest.submissionReceived) thenReturn Some(Instant.now())
      when(registrationRepository.set(any())).thenThrow(emulatedFailure)

      val caught = intercept[RuntimeException] {
        registrationService.amend(registrationRequest).futureValue
      }

      caught mustBe emulatedFailure
    }
  }

}
