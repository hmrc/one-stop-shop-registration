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
import connectors.{EnrolmentsConnector, RegistrationConnector}
import models.InsertResult.{AlreadyExists, InsertSucceeded}
import models._
import models.enrolments.EtmpEnrolmentResponse
import models.etmp.EtmpRegistrationStatus
import models.exclusions.ExcludedTrader
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers.running
import repositories.{RegistrationRepository, RegistrationStatusRepository}
import services.exclusions.ExclusionService
import testutils.RegistrationData
import testutils.RegistrationData.registration
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class RegistrationServiceEtmpImplSpec extends BaseSpec with BeforeAndAfterEach {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private val registrationRequest = RegistrationData.toRegistrationRequest(RegistrationData.registration)
  private val registrationConnector = mock[RegistrationConnector]
  private val enrolmentsConnector = mock[EnrolmentsConnector]
  private val registrationRepository = mock[RegistrationRepository]
  private val registrationStatusRepository = mock[RegistrationStatusRepository]
  private val retryService= mock[RetryService]
  private val appConfig = mock[AppConfig]

  private val exclusionService = mock[ExclusionService]
  private val registrationService = new RegistrationServiceEtmpImpl(registrationConnector, enrolmentsConnector,
    registrationRepository, registrationStatusRepository, retryService, appConfig, exclusionService, stubClock)

  override def beforeEach(): Unit = {
    reset(registrationConnector)
    reset(registrationRepository)
    reset(registrationStatusRepository)
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

      "duplicateRegistrationIntoRepository.disabled" - {

        "must create a registration from the request, save it and return the result of the save operation" in {

          when(enrolmentsConnector.confirmEnrolment(any())(any())) thenReturn Future.successful(HttpResponse(204, ""))
          when(appConfig.duplicateRegistrationIntoRepository) thenReturn false
          when(registrationConnector.create(any())) thenReturn Future.successful(
            Right(EtmpEnrolmentResponse(LocalDateTime.now(), vrn.vrn, "test")))
          when(registrationRepository.insert(any())) thenReturn successful(InsertSucceeded)
          when(registrationStatusRepository.delete(any())) thenReturn Future.successful(true)
          when(registrationStatusRepository.insert(any())) thenReturn successful(InsertSucceeded)

          registrationService.createRegistration(registrationRequest).futureValue mustEqual InsertSucceeded
          verify(registrationRepository, times(0)).insert(any())
        }

        "must return Already Exists when connector returns EtmpEnrolmentError with code 007" in {
          when(enrolmentsConnector.confirmEnrolment(any())(any())) thenReturn Future.successful(HttpResponse(204, ""))
          when(appConfig.duplicateRegistrationIntoRepository) thenReturn false
          when(registrationConnector.create(any())) thenReturn Future.successful(Left(EtmpEnrolmentError("007", "error")))
          when(registrationRepository.insert(any())) thenReturn successful(AlreadyExists)

          registrationService.createRegistration(registrationRequest).futureValue mustEqual AlreadyExists
          verify(registrationRepository, times(0)).insert(any())
        }

        "must throw EtmpException when connector returns any other error" in {

          when(enrolmentsConnector.confirmEnrolment(any())(any())) thenReturn Future.successful(HttpResponse(204, ""))
          when(appConfig.duplicateRegistrationIntoRepository) thenReturn false
          when(registrationConnector.create(any())) thenReturn Future.successful(Left(ServiceUnavailable))

        whenReady(registrationService.createRegistration(registrationRequest).failed) {
          exp => exp mustBe EtmpException(s"There was an error creating Registration enrolment from ETMP: ${ServiceUnavailable.body}")
        }
        verify(registrationRepository, times(0)).insert(any())
      }
    }

      "duplicateRegistrationIntoRepository.enabled" - {

        "must create a registration from the request, save it and return the result of the save operation" in {

          when(enrolmentsConnector.confirmEnrolment(any())(any())) thenReturn Future.successful(HttpResponse(204, ""))
          when(appConfig.duplicateRegistrationIntoRepository) thenReturn true
          when(registrationConnector.create(any())) thenReturn Future.successful(
            Right(EtmpEnrolmentResponse(LocalDateTime.now(), vrn.vrn, "test")))
          when(registrationRepository.insert(any())) thenReturn successful(InsertSucceeded)
          when(registrationStatusRepository.delete(any())) thenReturn Future.successful(true)
          when(registrationStatusRepository.insert(any())) thenReturn successful(InsertSucceeded)
          when(retryService.getEtmpRegistrationStatus(any(),any(),any())) thenReturn successful(EtmpRegistrationStatus.Success)

          registrationService.createRegistration(registrationRequest).futureValue mustEqual InsertSucceeded
          verify(registrationRepository, times(1)).insert(any())
        }

        "must return an error when repository returns an error" in {

          when(enrolmentsConnector.confirmEnrolment(any())(any())) thenReturn Future.successful(HttpResponse(204, ""))
          when(appConfig.duplicateRegistrationIntoRepository) thenReturn true
          when(registrationConnector.create(any())) thenReturn Future.successful(
            Right(EtmpEnrolmentResponse(LocalDateTime.now(), vrn.vrn, "test")))
          when(registrationRepository.insert(any())) thenReturn successful(AlreadyExists)
          when(registrationStatusRepository.delete(any())) thenReturn Future.successful(true)
          when(registrationStatusRepository.insert(any())) thenReturn successful(InsertSucceeded)

          registrationService.createRegistration(registrationRequest).futureValue mustEqual AlreadyExists
          verify(registrationRepository, times(1)).insert(any())
        }

        "must return Already Exists when connector returns EtmpEnrolmentError with code 007" in {
          when(enrolmentsConnector.confirmEnrolment(any())(any())) thenReturn Future.successful(HttpResponse(204, ""))
          when(appConfig.duplicateRegistrationIntoRepository) thenReturn true
          when(registrationConnector.create(any())) thenReturn Future.successful(Left(EtmpEnrolmentError("007", "error")))

          registrationService.createRegistration(registrationRequest).futureValue mustEqual AlreadyExists
          verify(registrationRepository, times(0)).insert(any())
        }

        "must throw EtmpException when connector returns any other error" in {

          when(enrolmentsConnector.confirmEnrolment(any())(any())) thenReturn Future.successful(HttpResponse(204, ""))
          when(appConfig.duplicateRegistrationIntoRepository) thenReturn true
          when(registrationConnector.create(any())) thenReturn Future.successful(Left(ServiceUnavailable))

          whenReady(registrationService.createRegistration(registrationRequest).failed) {
            exp => exp mustBe EtmpException(s"There was an error creating Registration enrolment from ETMP: ${ServiceUnavailable.body}")
          }
          verify(registrationRepository, times(0)).insert(any())
        }

        "must throw EtmpException when the retryService returns an error" in {

          when(enrolmentsConnector.confirmEnrolment(any())(any())) thenReturn Future.successful(HttpResponse(204, ""))
          when(appConfig.duplicateRegistrationIntoRepository) thenReturn true
          when(registrationConnector.create(any())) thenReturn Future.successful(
            Right(EtmpEnrolmentResponse(LocalDateTime.now(), vrn.vrn, "test")))

          when(registrationStatusRepository.delete(any())) thenReturn Future.successful(true)
          when(registrationStatusRepository.insert(any())) thenReturn successful(InsertSucceeded)

          when(retryService.getEtmpRegistrationStatus(any(), any(), any())) thenReturn successful(EtmpRegistrationStatus.Error)

          whenReady(registrationService.createRegistration(registrationRequest).failed) {
            exp => exp mustBe EtmpException(s"Failed to add enrolment")
          }
          verify(registrationRepository, times(0)).insert(any())
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

}
