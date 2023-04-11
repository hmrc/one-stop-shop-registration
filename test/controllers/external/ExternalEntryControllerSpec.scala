package controllers.external

import base.BaseSpec
import generators.Generators
import models.external.{ExternalEntryUrlResponse, ExternalRequest, ExternalResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.inject
import play.api.libs.json.{JsNull, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.external.ExternalEntryService

import scala.concurrent.Future

class ExternalEntryControllerSpec
  extends BaseSpec
    with ScalaCheckPropertyChecks
    with Generators {

  private val externalRequest = ExternalRequest("BTA", "exampleurl")

  ".onExternal" - {

    "when correct ExternalRequest is posted" - {
      "must respond with OK(IndexController.onPageLoad().url)" in {
        val mockExternalService = mock[ExternalEntryService]
        val url = "/pay-vat-on-goods-sold-to-eu/northern-ireland-register"

        when(mockExternalService.getExternalResponse(any(), any(), any())) thenReturn
          Future.successful(ExternalResponse(url))

        val application = applicationBuilder
          .overrides(inject.bind[ExternalEntryService].toInstance(mockExternalService))
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.external.routes.ExternalEntryController.onExternal().url)
            .withJsonBody(
              Json.toJson(externalRequest)
            )

          val result = route(application, request).value
          status(result) mustBe OK
          contentAsJson(result).as[ExternalResponse] mustBe ExternalResponse(url)
        }
      }

    }


    "must respond with BadRequest" - {
      "when no body provided" in {
        val application = applicationBuilder
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.external.routes.ExternalEntryController.onExternal().url)
            .withJsonBody(JsNull)

          val result = route(application, request).value
          status(result) mustBe BAD_REQUEST
        }
      }

      "when malformed body provided" in {
        val application = applicationBuilder
          .build()

        running(application) {
          val request = FakeRequest(POST, controllers.external.routes.ExternalEntryController.onExternal().url)
            .withJsonBody(Json.toJson("wrong body"))

          val result = route(application, request).value
          status(result) mustBe BAD_REQUEST
        }
      }
    }

  }

  ".getExternalEntry" - {

    "when correct request with authorization" - {
      "must respond with correct url when present" in {
        val mockExternalService = mock[ExternalEntryService]
        val url = "/pay-vat-on-goods-sold-to-eu/northern-ireland-register"

        when(mockExternalService.getSavedResponseUrl(any())) thenReturn
          Future.successful(Some(url))

        val application = applicationBuilder
          .overrides(inject.bind[ExternalEntryService].toInstance(mockExternalService))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.external.routes.ExternalEntryController.getExternalEntry().url)
          val result = route(application, request).value
          status(result) mustBe OK
          contentAsJson(result).as[ExternalEntryUrlResponse] mustBe ExternalEntryUrlResponse(Some(url))
        }
      }

      "must respond with none when no url present" in {
        val mockExternalService = mock[ExternalEntryService]

        when(mockExternalService.getSavedResponseUrl(any())) thenReturn
          Future.successful(None)

        val application = applicationBuilder
          .overrides(inject.bind[ExternalEntryService].toInstance(mockExternalService))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.external.routes.ExternalEntryController.getExternalEntry().url)

          val result = route(application, request).value
          status(result) mustBe OK
          contentAsJson(result).as[ExternalEntryUrlResponse] mustBe ExternalEntryUrlResponse(None)
        }
      }

    }

  }

}

