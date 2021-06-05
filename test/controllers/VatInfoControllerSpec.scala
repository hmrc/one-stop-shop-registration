package controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import base.BaseSpec
import connectors.DesConnector
import models.des._

import java.time.LocalDate
import scala.concurrent.Future

class VatInfoControllerSpec extends BaseSpec {

  "get" - {

    "must return OK and vat information when the connector returns vat info" in {

      val vatInfo = VatCustomerInfo(
        registrationDate = Some(LocalDate.now),
        address          = DesAddress("line1", None, None, None, None, Some("AA11 1AA"), "GB"),
        partOfVatGroup   = Some(false),
        organisationName = Some("Foo")
      )

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
