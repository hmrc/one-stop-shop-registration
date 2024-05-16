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
import com.codahale.metrics.Timer
import config.AppConfig
import connectors.{EnrolmentsConnector, GetVatInfoConnector, RegistrationConnector}
import controllers.actions.AuthorisedMandatoryVrnRequest
import metrics.ServiceMetrics
import models._
import models.core.{EisDisplayErrorDetail, EisDisplayErrorResponse, Match, MatchType}
import models.enrolments.EtmpEnrolmentResponse
import models.etmp._
import models.exclusions.ExcludedTrader
import models.repository.AmendResult.AmendSucceeded
import models.repository.InsertResult.{AlreadyExists, InsertSucceeded}
import org.apache.pekko.http.scaladsl.util.FastFuture.successful
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import repositories.{CachedRegistrationRepository, RegistrationStatusRepository}
import testutils.RegistrationData
import testutils.RegistrationData.{displayRegistration, fromEtmpRegistration, wrappedCachedRegistration}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationServiceEtmpImplSpec extends BaseSpec with BeforeAndAfterEach {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private val registrationRequest = RegistrationData.toRegistrationRequest(RegistrationData.registration)
  private val registrationConnector = mock[RegistrationConnector]
  private val enrolmentsConnector = mock[EnrolmentsConnector]
  private val getVatInfoConnector = mock[GetVatInfoConnector]
  private val registrationStatusRepository = mock[RegistrationStatusRepository]
  private val cachedRegistrationRepository = mock[CachedRegistrationRepository]
  private val retryService = mock[RetryService]
  private val appConfig = mock[AppConfig]
  private val serviceMetrics: ServiceMetrics = mock[ServiceMetrics]

  private val auditService = mock[AuditService]

  private val coreValidationService = mock[CoreValidationService]

  private val registrationService = new RegistrationServiceEtmpImpl(
    registrationConnector,
    enrolmentsConnector,
    getVatInfoConnector,
    registrationStatusRepository,
    cachedRegistrationRepository,
    retryService,
    appConfig,
    auditService,
    coreValidationService,
    stubClock
  )

  implicit private lazy val ar: AuthorisedMandatoryVrnRequest[AnyContent] = AuthorisedMandatoryVrnRequest(FakeRequest(), userId, vrn)

  override def beforeEach(): Unit = {
    reset(registrationConnector)
    reset(getVatInfoConnector)
    reset(registrationStatusRepository)
    reset(cachedRegistrationRepository)
    reset(appConfig)
    reset(serviceMetrics)
    reset(auditService)

    when(serviceMetrics.startTimer(any()))
      .thenReturn(new Timer().time)
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

      "must create a registration from the request, save it and return the result of the save operation and clear cache if enabled" in {

        when(enrolmentsConnector.confirmEnrolment(any())(any())) thenReturn Future.successful(HttpResponse(204, ""))
        when(appConfig.registrationCacheEnabled) thenReturn true
        doNothing().when(auditService).audit(any())(any(), any())
        when(registrationConnector.create(any())) thenReturn Future.successful(
          Right(EtmpEnrolmentResponse(LocalDateTime.now(), vrn.vrn, "test")))
        when(registrationStatusRepository.delete(any())) thenReturn Future.successful(true)
        when(registrationStatusRepository.insert(any())) thenReturn successful(InsertSucceeded)
        when(cachedRegistrationRepository.clear(any())) thenReturn Future.successful(true)
        when(retryService.getEtmpRegistrationStatus(any(), any(), any())) thenReturn successful(EtmpRegistrationStatus.Success)

        registrationService.createRegistration(registrationRequest).futureValue mustEqual InsertSucceeded
        verify(auditService, times(1)).audit(any())(any(), any())
        verify(cachedRegistrationRepository, times(1)).clear(any())
      }

      "must create a registration from the request, save it and return the result of the save operation and not clear cache if disabled" in {

        when(enrolmentsConnector.confirmEnrolment(any())(any())) thenReturn Future.successful(HttpResponse(204, ""))
        when(appConfig.registrationCacheEnabled) thenReturn false
        doNothing().when(auditService).audit(any())(any(), any())
        when(registrationConnector.create(any())) thenReturn Future.successful(
          Right(EtmpEnrolmentResponse(LocalDateTime.now(), vrn.vrn, "test")))
        when(registrationStatusRepository.delete(any())) thenReturn Future.successful(true)
        when(registrationStatusRepository.insert(any())) thenReturn successful(InsertSucceeded)
        when(retryService.getEtmpRegistrationStatus(any(), any(), any())) thenReturn successful(EtmpRegistrationStatus.Success)

        registrationService.createRegistration(registrationRequest).futureValue mustEqual InsertSucceeded
        verify(auditService, times(1)).audit(any())(any(), any())
        verifyNoInteractions(cachedRegistrationRepository)
      }

      "must return Already Exists when connector returns EtmpEnrolmentError with code 007" in {
        when(enrolmentsConnector.confirmEnrolment(any())(any())) thenReturn Future.successful(HttpResponse(204, ""))
        doNothing().when(auditService).audit(any())(any(), any())
        when(registrationConnector.create(any())) thenReturn Future.successful(Left(EtmpEnrolmentError("007", "error")))

        registrationService.createRegistration(registrationRequest).futureValue mustEqual AlreadyExists
        verify(auditService, times(1)).audit(any())(any(), any())
      }

      "must throw EtmpException when connector returns any other error" in {

        when(enrolmentsConnector.confirmEnrolment(any())(any())) thenReturn Future.successful(HttpResponse(204, ""))
        doNothing().when(auditService).audit(any())(any(), any())
        when(registrationConnector.create(any())) thenReturn Future.successful(Left(ServiceUnavailable))

        whenReady(registrationService.createRegistration(registrationRequest).failed) {
          exp => exp mustBe EtmpException(s"There was an error creating Registration enrolment from ETMP: ${ServiceUnavailable.body}")
        }
        verify(auditService, times(1)).audit(any())(any(), any())
      }

      "must throw EtmpException when the retryService returns an error" in {

        when(enrolmentsConnector.confirmEnrolment(any())(any())) thenReturn Future.successful(HttpResponse(204, ""))
        doNothing().when(auditService).audit(any())(any(), any())
        when(registrationConnector.create(any())) thenReturn Future.successful(
          Right(EtmpEnrolmentResponse(LocalDateTime.now(), vrn.vrn, "test")))

        when(registrationStatusRepository.delete(any())) thenReturn Future.successful(true)
        when(registrationStatusRepository.insert(any())) thenReturn successful(InsertSucceeded)

        when(retryService.getEtmpRegistrationStatus(any(), any(), any())) thenReturn successful(EtmpRegistrationStatus.Error)

        whenReady(registrationService.createRegistration(registrationRequest).failed) {
          exp => exp mustBe EtmpException(s"Failed to add enrolment")
        }
        verify(auditService, times(1)).audit(any())(any(), any())
      }

    }
  }

  ".get" - {

    "must return Some(registration) when both connectors return right and no cache value" in {

      val searchSchemeMatch: Match = Match(
        matchType = MatchType.OtherMSNETPQuarantinedNETP,
        traderId = "123456789",
        intermediary = None,
        memberState = "DE",
        exclusionStatusCode = None,
        exclusionDecisionDate = None,
        exclusionEffectiveDate = None,
        nonCompliantReturns = None,
        nonCompliantPayments = None
      )

      when(appConfig.registrationCacheEnabled) thenReturn true
      when(cachedRegistrationRepository.get(any())) thenReturn Future.successful(None)
      when(registrationConnector.get(any())) thenReturn Future.successful(Right(displayRegistration))
      when(getVatInfoConnector.getVatCustomerDetails(any())(any())) thenReturn Future.successful(Right(vatCustomerInfo))
      when(coreValidationService.searchScheme(any(), any(), any(), any())(any())) thenReturn Future.successful(Some(searchSchemeMatch))

      registrationService.get(Vrn("123456789")).futureValue mustBe Some(fromEtmpRegistration)
      verify(registrationConnector, times(1)).get(Vrn("123456789"))
      verify(getVatInfoConnector, times(1)).getVatCustomerDetails(Vrn("123456789"))
      verify(cachedRegistrationRepository, times(1)).get(any())
    }

    "must return cached registration value when cached" in {
      when(appConfig.registrationCacheEnabled) thenReturn true
      when(cachedRegistrationRepository.get(any())) thenReturn Future.successful(Some(wrappedCachedRegistration))
      registrationService.get(Vrn("123456789")).futureValue mustBe Some(fromEtmpRegistration)
      verifyNoInteractions(registrationConnector)
      verifyNoInteractions(getVatInfoConnector)
      verify(cachedRegistrationRepository, times(1)).get(any())
    }

    "must return Some(registration) when both connectors return right and doesn't call cache when not enabled" in {

      when(appConfig.registrationCacheEnabled) thenReturn false
      when(registrationConnector.get(any())) thenReturn Future.successful(Right(displayRegistration))
      when(getVatInfoConnector.getVatCustomerDetails(any())(any())) thenReturn Future.successful(Right(vatCustomerInfo))
      registrationService.get(Vrn("123456789")).futureValue mustBe Some(fromEtmpRegistration)
      verify(registrationConnector, times(1)).get(Vrn("123456789"))
      verify(getVatInfoConnector, times(1)).getVatCustomerDetails(Vrn("123456789"))
      verifyNoInteractions(cachedRegistrationRepository)
    }

    "when trader is excluded" - {

      val excludedTrader: ExcludedTrader = ExcludedTrader(vrn, 4, period, LocalDate.parse("2021-07-01"))
      val etmpExclusion: EtmpExclusion = EtmpExclusion(
        exclusionReason = EtmpExclusionReason.FailsToComply,
        effectiveDate = LocalDate.parse("2021-07-01"),
        decisionDate = LocalDate.parse("2021-09-30"),
        quarantine = true
      )

      val displayRegistrationWithExclusion: EtmpDisplayRegistration =
        displayRegistration
          .copy(schemeDetails = displayRegistration.schemeDetails.copy(
            exclusions = Seq(etmpExclusion)
          ))

      "must return Some(registration) when both connectors return right" in {
        when(registrationConnector.get(any())) thenReturn Future.successful(Right(displayRegistrationWithExclusion))
        when(getVatInfoConnector.getVatCustomerDetails(any())(any())) thenReturn Future.successful(Right(vatCustomerInfo))
        when(coreValidationService.searchScheme(any(), any(), any(), any())(any())) thenReturn Future.successful(None)
        registrationService.get(Vrn("123456789")).futureValue mustBe Some(fromEtmpRegistration.copy(excludedTrader = Some(excludedTrader)))
        verify(registrationConnector, times(1)).get(Vrn("123456789"))
        verify(getVatInfoConnector, times(1)).getVatCustomerDetails(Vrn("123456789"))
      }
    }

    "must return an exception when no customer VAT details are found" in {
      when(registrationConnector.get(any())) thenReturn Future.successful(Right(displayRegistration))
      when(getVatInfoConnector.getVatCustomerDetails(any())(any())) thenReturn Future.successful(Left(NotFound))
      whenReady(registrationService.get(Vrn("123456789")).failed) {
        exp => exp mustBe a[Exception]
      }
      verify(registrationConnector, times(1)).get(Vrn("123456789"))
      verify(getVatInfoConnector, times(1)).getVatCustomerDetails(Vrn("123456789"))

    }

    "must return a None when the connector returns Left Eis display error with code 098" in {
      when(registrationConnector.get(any())) thenReturn Future.successful(Left(EisDisplayRegistrationError(EisDisplayErrorResponse(EisDisplayErrorDetail("correlationId1", "089", "error message", "timestamp")))))
      registrationService.get(Vrn("123456789")).futureValue mustBe None
      verify(registrationConnector, times(1)).get(Vrn("123456789"))
    }

    "must return an ETMP Exception when the Registration Connector returns Left(error)" in {
      when(registrationConnector.get(any())) thenReturn Future.successful(Left(ServiceUnavailable))
      whenReady(registrationService.get(Vrn("123456789")).failed) {
        exp => exp mustBe EtmpException(s"There was an error getting Registration from ETMP: ${ServiceUnavailable.body}")
      }
      verify(registrationConnector, times(1)).get(Vrn("123456789"))
    }

  }

  ".amendRegistration" - {

    "duplicateRegistrationIntoRepository.disabled" - {

      "must create a registration from the request, save it and return the result of the save operation" in {

        doNothing().when(auditService).audit(any())(any(), any())
        when(registrationConnector.amendRegistration(any())) thenReturn Future.successful(
          Right(AmendRegistrationResponse(LocalDateTime.now(), "formBundle1", vrn.vrn, "bpnumber-1")))
        when(serviceMetrics.startTimer(any())).thenReturn(new Timer().time)

        registrationService.amend(registrationRequest).futureValue mustEqual AmendSucceeded
        verify(auditService, times(1)).audit(any())(any(), any())
      }

      "must throw EtmpException when connector an error" in {

        doNothing().when(auditService).audit(any())(any(), any())
        when(registrationConnector.amendRegistration(any())) thenReturn Future.successful(Left(ServiceUnavailable))

        whenReady(registrationService.amend(registrationRequest).failed) {
          exp => exp mustBe EtmpException(s"There was an error amending Registration from ETMP: ${ServiceUnavailable.getClass} ${ServiceUnavailable.body}")
        }
        verify(auditService, times(1)).audit(any())(any(), any())
      }
    }

    "duplicateRegistrationIntoRepository.enabled" - {

      "must create a registration from the request, save it and return the result of the save operation and not touch cache when disabled" in {

        when(appConfig.registrationCacheEnabled) thenReturn false
        doNothing().when(auditService).audit(any())(any(), any())
        when(registrationConnector.amendRegistration(any())) thenReturn Future.successful(
          Right(AmendRegistrationResponse(LocalDateTime.now(), "formBundle1", vrn.vrn, "bpnumber-1")))
        when(serviceMetrics.startTimer(any())).thenReturn(new Timer().time)

        registrationService.amend(registrationRequest).futureValue mustEqual AmendSucceeded
        verify(auditService, times(1)).audit(any())(any(), any())
        verifyNoInteractions(cachedRegistrationRepository)
      }

      "must create a registration from the request, save it and return the result of the save operation and clear cache when enabled" in {

        when(appConfig.registrationCacheEnabled) thenReturn true
        doNothing().when(auditService).audit(any())(any(), any())
        when(registrationConnector.amendRegistration(any())) thenReturn Future.successful(
          Right(AmendRegistrationResponse(LocalDateTime.now(), "formBundle1", vrn.vrn, "bpnumber-1")))
        when(serviceMetrics.startTimer(any())).thenReturn(new Timer().time)
        when(cachedRegistrationRepository.clear(any())) thenReturn Future.successful(true)

        registrationService.amend(registrationRequest).futureValue mustEqual AmendSucceeded
        verify(auditService, times(1)).audit(any())(any(), any())
        verify(cachedRegistrationRepository, times(1)).clear(any())
      }

      "must throw EtmpException when connector returns an error" in {

        doNothing().when(auditService).audit(any())(any(), any())
        when(registrationConnector.amendRegistration(any())) thenReturn Future.successful(Left(ServiceUnavailable))

        whenReady(registrationService.amend(registrationRequest).failed) {
          exp => exp mustBe EtmpException(s"There was an error amending Registration from ETMP: ${ServiceUnavailable.getClass} ${ServiceUnavailable.body}")
        }
        verify(auditService, times(1)).audit(any())(any(), any())
      }

    }
  }

}
