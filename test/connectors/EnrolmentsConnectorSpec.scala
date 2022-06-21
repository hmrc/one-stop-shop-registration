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
  private val enrolmentKey = s"HMRC-OSS-ORG~VRN~${vrn}"
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

}
