package connectors

import base.BaseSpec
import com.github.tomakehurst.wiremock.client.WireMock._
import models.DesAddress
import models.des._
import org.scalacheck.Gen
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running

import java.time.LocalDate

class DesConnectorSpec extends BaseSpec with WireMockHelper {

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.des.port" -> server.port)
      .build()

  private val desUrl = s"/one-stop-shop-registration-stub/vat/customer/vrn/${vrn.value}/information"

  "getVatCustomerInfo" - {

    "when the server returns OK and a recognised payload" - {

      "must return a Right(VatCustomerInfo)" in {

        val app = application

        val responseJson =
          """{
            |  "approvedInformation": {
            |    "customerDetails": {
            |      "effectiveRegistrationDate": "2000-01-01",
            |      "partyType": "Z2",
            |      "organisationName": "Foo"
            |    },
            |    "PPOB": {
            |      "address": {
            |        "line1": "line 1",
            |        "line2": "line 2",
            |        "postCode": "AA11 1AA",
            |        "countryCode": "GB"
            |      }
            |    }
            |  }
            |}""".stripMargin

        server.stubFor(
          get(urlEqualTo(desUrl))
            .willReturn(ok(responseJson))
        )

        running(app) {
          val connector = app.injector.instanceOf[DesConnector]
          val result = connector.getVatCustomerDetails(vrn).futureValue

          val expectedResult = VatCustomerInfo(
            registrationDate = Some(LocalDate.of(2000, 1, 1)),
            address          = DesAddress("line 1", Some("line 2"), None, None, None, Some("AA11 1AA"), "GB"),
            partOfVatGroup   = Some(true),
            organisationName = Some("Foo")
          )

          result mustEqual Right(expectedResult)
        }
      }

      "must return Left(NotFound) when the server returns NOT_FOUND" in {

        val app = application

        server.stubFor(
          get(urlEqualTo(desUrl))
            .willReturn(notFound())
        )

        running(app) {

          val connector = app.injector.instanceOf[DesConnector]
          val result = connector.getVatCustomerDetails(vrn).futureValue

          result mustEqual Left(NotFound)
        }
      }

      "must return Left(InvalidVrn) when the server returns BAD_REQUEST" in {

        val app = application

        server.stubFor(
          get(urlEqualTo(desUrl))
            .willReturn(badRequest())
        )

        running(app) {

          val connector = app.injector.instanceOf[DesConnector]
          val result = connector.getVatCustomerDetails(vrn).futureValue

          result mustEqual Left(InvalidVrn)
        }
      }


      "must return Left(ServiceUnavailable) when the server returns SERVICE_UNAVAILABLE" in {

        val app = application

        server.stubFor(
          get(urlEqualTo(desUrl))
            .willReturn(serviceUnavailable())
        )

        running(app) {

          val connector = app.injector.instanceOf[DesConnector]
          val result = connector.getVatCustomerDetails(vrn).futureValue

          result mustEqual Left(ServiceUnavailable)
        }
      }


      "must return Left(ServerError) when the server returns INTERNAL_SERVER_ERROR" in {

        val app = application

        server.stubFor(
          get(urlEqualTo(desUrl))
            .willReturn(serverError())
        )

        running(app) {

          val connector = app.injector.instanceOf[DesConnector]
          val result = connector.getVatCustomerDetails(vrn).futureValue

          result mustEqual Left(ServerError)
        }
      }


      "must return Left(InvalidJson) when the server returns OK with a payload that cannot be parsed" in {

        val app = application

        val responseJson = """{ "foo": "bar" }"""

        server.stubFor(
          get(urlEqualTo(desUrl))
            .willReturn(ok(responseJson))
        )

        running(app) {

          val connector = app.injector.instanceOf[DesConnector]
          val result = connector.getVatCustomerDetails(vrn).futureValue

          result mustEqual Left(InvalidJson)
        }
      }

      "must return Left(UnexpectedResponse) when the server returns an unexpected response code" in {

        val app = application

        val status = Gen.oneOf(401, 402, 403, 501, 502).sample.value

        server.stubFor(
          get(urlEqualTo(desUrl))
            .willReturn(aResponse().withStatus(status))
        )

        running(app) {

          val connector = app.injector.instanceOf[DesConnector]
          val result = connector.getVatCustomerDetails(vrn).futureValue

          result mustEqual Left(UnexpectedResponseStatus(status, s"Unexpected response from DES, received status $status"))
        }
      }
    }
  }
}
