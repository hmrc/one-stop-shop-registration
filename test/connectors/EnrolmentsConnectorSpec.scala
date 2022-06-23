package connectors

import base.BaseSpec
import com.github.tomakehurst.wiremock.client.WireMock._
import models._
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, NO_CONTENT, UNAUTHORIZED}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.running


class EnrolmentsConnectorSpec extends BaseSpec with WireMockHelper {

  private val basePath = "tax-enrolments/"

  val errorResponseBody = "Error"
  val response: JsValue = Json.toJson(TaxEnrolmentErrorResponse(NOT_FOUND.toString, errorResponseBody))

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.enrolments.host" -> "127.0.0.1",
        "microservice.services.enrolments.port" -> server.port,
        "microservice.services.enrolments.authorizationToken" -> "auth-token",
        "microservice.services.enrolments.basePath" -> basePath
      )
      .build()

  private val subscriptionId = "123456789"

  private val url = s"/${basePath}subscriptions/$subscriptionId/subscriber"

  ".confirmEnrolment" - {


    "must return an HttpResponse with status NoContent when the server returns NoContent" in {

      val app = application

      server.stubFor(
        put(urlEqualTo(url))
          .withHeader("Authorization", equalTo("Bearer auth-token"))
          .willReturn(aResponse().withStatus(NO_CONTENT))
      )

      running(app) {
        val connector = app.injector.instanceOf[EnrolmentsConnector]
        val result = connector.confirmEnrolment(subscriptionId).futureValue


        result.status mustEqual NO_CONTENT
      }
    }


    Seq(BAD_REQUEST, UNAUTHORIZED).foreach {
      status =>
        s"must return an Exception when the server returns $status" in {

          val app = application

          server.stubFor(
            put(urlEqualTo(url))
              .withHeader("Authorization", equalTo("Bearer auth-token"))
              .willReturn(aResponse().withStatus(status))
          )

          running(app) {
            val connector = app.injector.instanceOf[EnrolmentsConnector]


            whenReady(connector.confirmEnrolment(subscriptionId).failed) {
              exp => exp mustBe a[Exception]
            }
          }
        }
    }

  }

}
