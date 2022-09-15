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

import base.BaseSpec
import config.AppConfig
import connectors.{EnrolmentsConnector, RegistrationConnector}
import models.InsertResult.{AlreadyExists, InsertSucceeded}
import models.enrolments.EtmpEnrolmentResponse
import models._
import models.exclusions.ExcludedTrader
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers.running
import services.exclusions.ExclusionService
import testutils.RegistrationData
import testutils.RegistrationData.registration
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class RegistrationServiceEtmpImplSpec extends BaseSpec with BeforeAndAfterEach {
  private val registrationRequest = RegistrationData.toRegistrationRequest(RegistrationData.registration)
  private val registrationConnector = mock[RegistrationConnector]
  private val enrolmentsConnector = mock[EnrolmentsConnector]
  private val appConfig = mock[AppConfig]

  private val exclusionService = mock[ExclusionService]
  private val registrationService = new RegistrationServiceEtmpImpl(registrationConnector, enrolmentsConnector, appConfig, exclusionService)

  override def beforeEach(): Unit = {
    reset(registrationConnector)
    reset(exclusionService)
    reset(appConfig)
    super.beforeEach()
  }

  "RegistrationServiceEtmpImpl is bound if the sendRegToEtmp toggle is true" in {
    val app =
      applicationBuilder
        .configure(
          "features.sendRegToEtmp" -> "true"
        )
        .build()

    running(app) {
      val service = app.injector.instanceOf[RegistrationService]
      service.getClass mustBe classOf[RegistrationServiceEtmpImpl]
    }
  }

  ".createRegistration" - {

    "enrolmentToggle.enabled" - {

      "must create a registration from the request, save it and return the result of the save operation" in {

        when(enrolmentsConnector.confirmEnrolment(any())) thenReturn Future.successful(HttpResponse(204, ""))
        when(appConfig.addEnrolment) thenReturn true
        when(registrationConnector.createWithEnrolment(any())) thenReturn Future.successful(
          Right(EtmpEnrolmentResponse(LocalDate.now(), vrn.vrn, "test")))

        registrationService.createRegistration(registrationRequest).futureValue mustEqual InsertSucceeded
      }

      "must return Already Exists when connector returns Conflict" in {
        when(enrolmentsConnector.confirmEnrolment(any())) thenReturn Future.successful(HttpResponse(204, ""))
        when(appConfig.addEnrolment) thenReturn true
        when(registrationConnector.createWithEnrolment(any())) thenReturn Future.successful(Left(Conflict))

        registrationService.createRegistration(registrationRequest).futureValue mustEqual AlreadyExists
      }

      "must return Already Exists when connector returns EtmpEnrolmentError with code 007" in {
        when(enrolmentsConnector.confirmEnrolment(any())) thenReturn Future.successful(HttpResponse(204, ""))
        when(appConfig.addEnrolment) thenReturn true
        when(registrationConnector.createWithEnrolment(any())) thenReturn Future.successful(Left(EtmpEnrolmentError("007", "error")))

        registrationService.createRegistration(registrationRequest).futureValue mustEqual AlreadyExists
      }

      "must throw EtmpException when connector returns any other error" in {

        when(enrolmentsConnector.confirmEnrolment(any())) thenReturn Future.successful(HttpResponse(204, ""))
        when(appConfig.addEnrolment) thenReturn true
        when(registrationConnector.createWithEnrolment(any())) thenReturn Future.successful(Left(ServiceUnavailable))

        whenReady(registrationService.createRegistration(registrationRequest).failed) {
          exp => exp mustBe EtmpException(s"There was an error creating Registration enrolment from ETMP: ${ServiceUnavailable.body}")
        }
      }
    }

    "enrolmentToggle.disabled" - {

      "must create a registration from the request, save it and return the result of the save operation" in {

        when(appConfig.addEnrolment) thenReturn false
        when(registrationConnector.create(any())) thenReturn Future.successful(Right(()))

        registrationService.createRegistration(registrationRequest).futureValue mustEqual InsertSucceeded
      }

      "must return Already Exists when connector returns conflict" in {

        when(appConfig.addEnrolment) thenReturn false
        when(registrationConnector.create(any())) thenReturn Future.successful(Left(Conflict))

        registrationService.createRegistration(registrationRequest).futureValue mustBe AlreadyExists
      }

      "must throw Exception when connector returns any other error" in {

        when(appConfig.addEnrolment) thenReturn false
        when(registrationConnector.create(any())) thenReturn Future.successful(Left(ServiceUnavailable))

        whenReady(registrationService.createRegistration(registrationRequest).failed) {
          exp => exp mustBe EtmpException(s"There was an error getting Registration from ETMP: ${ServiceUnavailable.body}")
        }
      }
    }
  }

  ".get" - {

    "must return a Some(registration) when the connector returns right" in {
      when(registrationConnector.get(any())) thenReturn Future.successful(Right(registration))
      registrationService.get(Vrn("123456789")).futureValue mustBe Some(registration)
      verify(registrationConnector, times(1)).get(Vrn("123456789"))
    }

    "when exclusion is enabled and trader is excluded" - {

      val excludedTrader: ExcludedTrader = ExcludedTrader(vrn, "HMRC", 4, period)

      "must return a Some(registration) when the connector returns right" in {
        when(registrationConnector.get(any())) thenReturn Future.successful(Right(registration))
        when(appConfig.exclusionsEnabled) thenReturn(true)
        when(exclusionService.findExcludedTrader(any())) thenReturn Future.successful(Some(excludedTrader))
        registrationService.get(Vrn("123456789")).futureValue mustBe Some(registration.copy(excludedTrader = Some(excludedTrader)))
        verify(registrationConnector, times(1)).get(Vrn("123456789"))
      }
    }

    "must return a None when the connector returns Left(error)" in {
      when(registrationConnector.get(any())) thenReturn Future.successful(Left(NotFound))
      registrationService.get(Vrn("123456789")).futureValue mustBe None
      verify(registrationConnector, times(1)).get(Vrn("123456789"))
    }
  }

  ".validate" - {
    "must make a call to the validate method in RegistrationConnector" in {
      when(registrationConnector.validateRegistration(any())) thenReturn Future.successful(Right(RegistrationValidationResult(true)))
      registrationService.validate(vrn).futureValue
      verify(registrationConnector, times(1)).validateRegistration(vrn)
    }
  }
}
