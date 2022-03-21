package connectors

import base.BaseSpec
import com.github.tomakehurst.wiremock.client.WireMock._
import models._
import org.scalacheck.Gen
import play.api.Application
import play.api.http.Status.NOT_FOUND
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier


class EnrolmentsConnectorSpec extends BaseSpec with WireMockHelper {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private val userId = "credId-123456789"
  private val enrolmentKey = "HMRC-OSS-ORG~VRN~123456789"
  private val basePath = "tax-enrolments/"

  private val enrolmentsUrl = s"/${basePath}users/$userId/enrolments/$enrolmentKey"

  val errorResponseBody = "Error"
  val response: JsValue = Json.toJson(TaxEnrolmentErrorResponse(NOT_FOUND.toString, errorResponseBody))

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.enrolments.host" -> "127.0.0.1",
        "microservice.services.enrolments.port" -> server.port,
        "microservice.services.des.authorizationToken" -> "auth-token",
        "microservice.services.enrolments.basePath" -> basePath
      )
      .build()

  "assignEnrolment" - {

    "must return Right() when a user is a assigned an enrolment" in {

      val app = application

      server.stubFor(
        get(urlEqualTo(enrolmentsUrl))
          .willReturn(created)
      )

      running(app) {
        val connector = app.injector.instanceOf[EnrolmentsConnector]
        val result = connector.assignEnrolment(userId, enrolmentKey).futureValue

        result mustBe Right()
      }
    }

    "must return Left with an error response body ???" in {

      val app = application

      server.stubFor(
        get(urlEqualTo(enrolmentsUrl))
          .willReturn(notFound.withBody(response.toString()))
      )

      running(app) {

        val connector = app.injector.instanceOf[EnrolmentsConnector]
        val result = connector.assignEnrolment(userId, enrolmentKey).futureValue

        result mustBe Left(TaxEnrolmentErrorResponse(NOT_FOUND.toString, errorResponseBody))
      }
    }

    "must return Left with an empty error response body ???" in {

      val app = application

      server.stubFor(
        get(urlEqualTo(enrolmentsUrl))
          .willReturn(notFound())
      )

      running(app) {

        val connector = app.injector.instanceOf[EnrolmentsConnector]
        val result = connector.assignEnrolment(userId, enrolmentKey).futureValue

        result mustBe Left(TaxEnrolmentErrorResponse(s"UNEXPECTED_404", "The response body was empty"))
      }
    }

    "must return Left(UnexpectedResponse) when the server returns an unexpected response code" in {

      val app = application

      val status = Gen.oneOf(401, 402, 403, 501, 502).sample.value

      server.stubFor(
        get(urlEqualTo(enrolmentsUrl))
          .willReturn(aResponse().withStatus(status).withBody(Json.toJson(errorResponseBody).toString()))
      )

      running(app) {

        val connector = app.injector.instanceOf[EnrolmentsConnector]
        val result = connector.assignEnrolment(userId, enrolmentKey).futureValue

        result mustBe Left(TaxEnrolmentErrorResponse(s"UNEXPECTED_${status}", Json.toJson(errorResponseBody).toString()))
      }
    }
  }
}
