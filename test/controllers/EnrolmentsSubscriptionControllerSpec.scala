package controllers

import base.BaseSpec
import connectors.EnrolmentsConnector
import models.etmp.EtmpRegistrationStatus
import models.RegistrationStatus
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.RegistrationStatusRepository
import services.RetryService
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class EnrolmentsSubscriptionControllerSpec extends BaseSpec with BeforeAndAfterEach {

  private val mockEnrolmentsConnector = mock[EnrolmentsConnector]
  private val mockRegistrationStatusRepository = mock[RegistrationStatusRepository]
  private val mockRetryService = mock[RetryService]


  override def beforeEach(): Unit = {
    reset(mockEnrolmentsConnector)
    reset(mockRegistrationStatusRepository)
    reset(mockRetryService)

    super.beforeEach()
  }

/*  "authoriseEnrolment" - {
    "must update status when successful" in {
    }

    "must update status when failure" in {
    }
  }*/

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
