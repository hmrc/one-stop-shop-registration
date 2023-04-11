package connectors

import base.BaseSpec
import com.github.tomakehurst.wiremock.client.WireMock._
import models._
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, CREATED, NO_CONTENT, NOT_FOUND, UNAUTHORIZED}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, JsValue}
import play.api.test.Helpers.running
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.util.UUID

class EnrolmentsConnectorSpec extends BaseSpec with WireMockHelper {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  val errorResponseBody = "Error"
  val response: JsValue = Json.toJson(TaxEnrolmentErrorResponse(NOT_FOUND.toString, errorResponseBody))

  ".confirmEnrolment" - {

    val basePath = "tax-enrolments/"

    def application: Application =
      new GuiceApplicationBuilder()
        .configure(
          "microservice.services.enrolments.host" -> "127.0.0.1",
          "microservice.services.enrolments.port" -> server.port,
          "microservice.services.enrolments.authorizationToken" -> "auth-token",
          "microservice.services.enrolments.basePath" -> basePath
        )
        .build()

    val subscriptionId = "123456789"
    val url = s"/${basePath}subscriptions/$subscriptionId/subscriber"

    "must return an HttpResponse with status NoContent when the server returns NoContent" in {

      val app = application

      server.stubFor(
        put(urlEqualTo(url))
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
        s"must return an Http response with $status when the server returns $status" in {

          val app = application

          server.stubFor(
            put(urlEqualTo(url))
              .willReturn(aResponse().withStatus(status))
          )

          running(app) {
            val connector = app.injector.instanceOf[EnrolmentsConnector]

            val result = connector.confirmEnrolment(subscriptionId).futureValue

            result.status mustEqual status
          }
        }
    }

  }

  ".es8" - {

    val basePath = "enrolment-store-proxy/"

    def application: Application =
      new GuiceApplicationBuilder()
        .configure(
          "microservice.services.enrolment-store-proxy.host" -> "127.0.0.1",
          "microservice.services.enrolment-store-proxy.port" -> server.port,
          "microservice.services.enrolment-store-proxy.authorizationToken" -> "auth-token",
          "microservice.services.enrolment-store-proxy.basePath" -> basePath
        )
        .build()

    val vrn = Vrn("123456789")
    val groupId = UUID.randomUUID().toString
    val userId = "987654321"
    val url = s"/${basePath}enrolment-store/groups/$groupId/enrolments/HMRC-OSS-ORG~${vrn.vrn}"
    val today = LocalDate.now()

    "must return an HttpResponse with status CREATED when the server returns NoContent" in {

      val app = application

      server.stubFor(
        post(urlEqualTo(url))
          .willReturn(aResponse().withStatus(CREATED))
      )

      running(app) {
        val connector = app.injector.instanceOf[EnrolmentsConnector]
        val result = connector.es8(groupId, vrn, userId, today).futureValue


        result.status mustEqual CREATED
      }
    }


    Seq(BAD_REQUEST, UNAUTHORIZED).foreach {
      status =>
        s"must return an Http response with $status when the server returns $status" in {

          val app = application

          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(aResponse().withStatus(status))
          )

          running(app) {
            val connector = app.injector.instanceOf[EnrolmentsConnector]

            val result = connector.es8(groupId, vrn, userId, today).futureValue

            result.status mustEqual status
          }
        }
    }

  }

}
