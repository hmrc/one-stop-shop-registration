package controllers

import base.BaseSpec
import connectors.EnrolmentsConnector
import models.etmp.EtmpRegistrationStatus
import models.RegistrationStatus
import models.enrolments.EnrolmentStatus
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.RegistrationStatusRepository
import services.RetryService
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class EnrolmentsSubscriptionControllerSpec extends BaseSpec with BeforeAndAfterEach with ScalaCheckPropertyChecks {

  private val mockEnrolmentsConnector = mock[EnrolmentsConnector]
  private val mockRegistrationStatusRepository = mock[RegistrationStatusRepository]
  private val mockRetryService = mock[RetryService]


  override def beforeEach(): Unit = {
    reset(mockEnrolmentsConnector)
    reset(mockRegistrationStatusRepository)
    reset(mockRetryService)

    super.beforeEach()
  }

  "authoriseEnrolment" - {
    "must handle successful enrolment correctly" in {
      val subscriptionId = "subscription-987654321"
      val enrolmentJson = Json.obj("state" -> "SUCCEEDED")

      when(mockRegistrationStatusRepository.set(any())) thenReturn Future.successful(RegistrationStatus(subscriptionId, EtmpRegistrationStatus.Success))

      val app =
        applicationBuilder
            .overrides(bind[RegistrationStatusRepository].toInstance(mockRegistrationStatusRepository))
            .build()

      running(app) {
        val request =
          FakeRequest(POST, routes.EnrolmentsSubscriptionController.authoriseEnrolment(subscriptionId).url)
              .withHeaders(CONTENT_TYPE -> "application/json")
              .withJsonBody(enrolmentJson)

        val result = route(app, request).value

        status(result) mustEqual NO_CONTENT
        verify(mockRegistrationStatusRepository).set(RegistrationStatus(subscriptionId, EtmpRegistrationStatus.Success))
      }
    }


    "must handle failed enrolment correctly" in {
      val subscriptionId = "subscription-987654321"

      val enrolmentStatus = arbitraryFailedEnrolmentStatus.arbitrary.sample.get

            val statusName = enrolmentStatus match {
              case EnrolmentStatus.Failure => EnrolmentStatus.Failure.jsonName
              case EnrolmentStatus.Enrolled => EnrolmentStatus.Enrolled.jsonName
              case EnrolmentStatus.EnrolmentError => EnrolmentStatus.EnrolmentError.jsonName
              case EnrolmentStatus.AuthRefreshed => EnrolmentStatus.AuthRefreshed.jsonName
              case _ => fail("Hit unexpected case for test")
            }

            val enrolmentJson = Json.obj("state" -> statusName)

            when(mockRegistrationStatusRepository.set(any())) thenReturn Future.successful(RegistrationStatus(subscriptionId, EtmpRegistrationStatus.Error))

            val app =
              applicationBuilder
                  .overrides(bind[RegistrationStatusRepository].toInstance(mockRegistrationStatusRepository))
                  .build()

            running(app) {
              val request =
                FakeRequest(POST, routes.EnrolmentsSubscriptionController.authoriseEnrolment(subscriptionId).url)
                    .withHeaders(CONTENT_TYPE -> "application/json")
                    .withJsonBody(enrolmentJson)

              val result = route(app, request).value

              status(result) mustEqual NO_CONTENT
              verify(mockRegistrationStatusRepository).set(RegistrationStatus(subscriptionId, EtmpRegistrationStatus.Error))
          }
      }
    }

    "confirmEnrolment" - {
      "must confirm the enrolment and reply NoContent when successful" in {

        val subscriptionId = "subscription-987654321"

        when(mockEnrolmentsConnector.confirmEnrolment(eqTo(subscriptionId))(any())) thenReturn Future.successful(HttpResponse(204, ""))
        when(mockRetryService.getEtmpRegistrationStatus(any(), any(), any())) thenReturn Future.successful(EtmpRegistrationStatus.Success)
        when(mockRegistrationStatusRepository.set(any())) thenReturn Future.successful(RegistrationStatus(subscriptionId, EtmpRegistrationStatus.Success))

        val app =
          applicationBuilder
              .overrides(bind[EnrolmentsConnector].toInstance(mockEnrolmentsConnector))
              .overrides(bind[RegistrationStatusRepository].toInstance(mockRegistrationStatusRepository))
              .overrides(bind[RetryService].toInstance(mockRetryService))
              .configure("features.fallbackEnrolment.traders.1.vrn" -> "123456789")
              .configure("features.fallbackEnrolment.traders.1.subscriptionId" -> subscriptionId)
              .build()

        running(app) {

          val request =
            FakeRequest(POST, routes.EnrolmentsSubscriptionController.confirmEnrolment().url)

          val result = route(app, request).value

          status(result) mustEqual NO_CONTENT
        }
      }

      "must response NotFound when subscriptionId doesn't exist in config" in {

        val app =
          applicationBuilder
              .overrides(bind[EnrolmentsConnector].toInstance(mockEnrolmentsConnector))
              .overrides(bind[RegistrationStatusRepository].toInstance(mockRegistrationStatusRepository))
              .overrides(bind[RetryService].toInstance(mockRetryService))
              .build()

        running(app) {

          val request =
            FakeRequest(POST, routes.EnrolmentsSubscriptionController.confirmEnrolment().url)

          val result = route(app, request).value

          status(result) mustEqual NOT_FOUND
        }

      }
    }

  }
