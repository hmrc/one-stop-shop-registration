package uk.gov.hmrc.onestopshopregistration.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.onestopshopregistration.base.BaseSpec
import uk.gov.hmrc.onestopshopregistration.connectors.DesConnector
import uk.gov.hmrc.onestopshopregistration.models.des._

import java.time.LocalDate
import scala.concurrent.Future

class VatInfoControllerSpec extends BaseSpec {

  "get" - {

    "must return OK and vat information when the connector returns vat info" in {

      val vatInfo = VatCustomerInfo(LocalDate.now, "AA11 1AA")
      val mockConnector = mock[DesConnector]
      when(mockConnector.getVatCustomerDetails(any())) thenReturn Future.successful(Right(vatInfo))

      val app = applicationBuilder.overrides(bind[DesConnector].toInstance(mockConnector)).build()

      running(app) {

        val request = FakeRequest(GET, routes.VatInfoController.get().url)
        val result = route(app, request).value

        status(result) mustEqual OK
        contentAsJson(result) mustEqual Json.toJson(vatInfo)
      }
    }

    "must return NotFound when the connector returns Not Found" in {

      val mockConnector = mock[DesConnector]
      when(mockConnector.getVatCustomerDetails(any())) thenReturn Future.successful(Left(NotFound))

      val app = applicationBuilder.overrides(bind[DesConnector].toInstance(mockConnector)).build()

      running(app) {

        val request = FakeRequest(GET, routes.VatInfoController.get().url)
        val result = route(app, request).value

        status(result) mustEqual NOT_FOUND
      }
    }

    "must return INTERNAL_SERVER_ERROR when the connector returns a failure other than Not Found" in {

      val response = Gen.oneOf(InvalidJson, ServerError, ServiceUnavailable, InvalidVrn).sample.value
      val mockConnector = mock[DesConnector]
      when(mockConnector.getVatCustomerDetails(any())) thenReturn Future.successful(Left(response))

      val app = applicationBuilder.overrides(bind[DesConnector].toInstance(mockConnector)).build()

      running(app) {

        val request = FakeRequest(GET, routes.VatInfoController.get().url)
        val result = route(app, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }
  }
}
