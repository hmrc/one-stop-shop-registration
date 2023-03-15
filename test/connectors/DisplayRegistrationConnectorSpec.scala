package connectors

import base.BaseSpec
import com.github.tomakehurst.wiremock.client.WireMock._
import models.etmp._
import models._
import org.scalacheck.Gen
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate

class DisplayRegistrationConnectorSpec extends BaseSpec with WireMockHelper {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private val url = s"/one-stop-shop-registration-stub/RESTAdapter/OSS/Subscription/${vrn.value}"

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.display-registration.host" -> "127.0.0.1",
        "microservice.services.display-registration.port" -> server.port,
        "microservice.services.display-registration.authorizationToken" -> "auth-token",
        "microservice.services.display-registration.environment" -> "test-environment"
      )
      .build()

  ".displayRegistration" - {

    "must return Right(DisplayRegistration) when server returns OK with a recognised payload" in {

      val app = application

      val responseJson =
        """{
          | "tradingNames" : [{
          |   "tradingName":"French Trading Company"
          | }],
          | "schemeDetails" : {
          |   "commencementDate" : "2023-01-01",
          |   "firstSaleDate" : "2023-01-25",
          |   "euRegistrationDetails" : [{
          |     "issuedBy" : "FR",
          |     "vatNumber" : "FR123456789",
          |     "taxIdentificationNumber" : "123456789",
          |     "fixedEstablishment" : true,
          |     "fixedEstablishmentTradingName" : "French Trading Company",
          |     "fixedEstablishmentAddressLine1" : "Line1",
          |     "fixedEstablishmentAddressLine2" : "Line2",
          |     "townOrCity" : "Town",
          |     "regionOrState" : "Region",
          |     "postcode" : "Postcode"
          |   }],
          |   "previousEURegistrationDetails" : [{
          |     "issuedBy" : "ES",
          |     "registrationNumber" : "ES123456789",
          |     "schemeType" : "IOSS with intermediary",
          |     "intermediaryNumber" : "IN7241234567"
          |   }],
          |   "onlineMarketPlace" : true,
          |   "websites" : [{
          |     "websiteAddress" : "www.testWebsite.com"
          |   }],
          |   "contactDetails" : {
          |     "contactNameOrBusinessAddress" : "Mr Test",
          |     "businessTelephoneNumber" : "1234567890",
          |     "businessEmailAddress" : "test@testEmail.com"
          |   },
          |   "nonCompliantReturns" : 1,
          |   "nonCompliantPayments" : 2
          | },
          | "bankDetails" : {
          |   "accountName" : "Bank Account Name",
          |   "bic" : "ABCDGB2A",
          |   "iban" : "GB33BUKB20201555555555"
          | }
          |}""".stripMargin

      server.stubFor(
        get(urlEqualTo(url))
          .withHeader("Authorization", equalTo("Bearer auth-token"))
          .withHeader("Environment", equalTo("test-environment"))
          .willReturn(ok(responseJson))
      )

      running(app) {

        val connector = application.injector.instanceOf[RegistrationConnector]

        val result = connector.get(vrn).futureValue

        val expectedResult = DisplayRegistration(
          tradingNames = Seq(
            EtmpTradingNames(
              tradingName = "French Trading Company"
            )
          ),
          schemeDetails = EtmpSchemeDetails(
            commencementDate = LocalDate.of(2023, 1, 1).format(dateFormatter),
            firstSaleDate = Some(LocalDate.of(2023, 1, 25).format(dateFormatter)),
            euRegistrationDetails = Seq(
              EtmpEuRegistrationDetails(
                countryOfRegistration = "FR",
                vatNumber = Some("FR123456789"),
                taxIdentificationNumber = Some("123456789"),
                fixedEstablishment = Some(true),
                tradingName = Some("French Trading Company"),
                fixedEstablishmentAddressLine1 = Some("Line1"),
                fixedEstablishmentAddressLine2 = Some("Line2"),
                townOrCity = Some("Town"),
                regionOrState = Some("Region"),
                postcode = Some("Postcode")
              )
            ),
            previousEURegistrationDetails = Seq(
              EtmpPreviousEURegistrationDetails(
                issuedBy = "ES",
                registrationNumber = "ES123456789",
                schemeType = SchemeType.IOSSWithIntermediary,
                intermediaryNumber = Some("IN7241234567")
              )
            ),
            onlineMarketPlace = true,
            websites = Seq(
              Website(
                websiteAddress = "www.testWebsite.com"
              )
            ),
            contactName = "Mr Test",
            businessTelephoneNumber = "1234567890",
            businessEmailId = "test@testEmail.com",
            nonCompliantReturns = Some(1),
            nonCompliantPayments = Some(2)
          ),
          bankDetails = BankDetails(
            accountName = "Bank Account Name",
            Some(bic),
            iban
          )
        )

        result mustBe Right(expectedResult)
      }
    }

    "must return Left(NotFound) when server returns NOT_FOUND" in {

      val app = application

      server.stubFor(
        get(urlEqualTo(url))
          .withHeader("Authorization", equalTo("Bearer auth-token"))
          .withHeader("Environment", equalTo("test-environment"))
          .willReturn(notFound())
      )

      running(app) {

        val connector = application.injector.instanceOf[RegistrationConnector]

        val result = connector.get(vrn).futureValue

        result mustBe Left(NotFound)
      }
    }

    "must return Left(ServiceUnavailable) when the server returns SERVICE_UNAVAILABLE" in {

      val app = application

      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(serviceUnavailable())
      )

      running(app) {

        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.get(vrn).futureValue

        result mustBe Left(ServiceUnavailable)
      }
    }

    "must return Left(ServerError) when the server returns INTERNAL_SERVER_ERROR" in {

      val app = application

      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(serverError())
      )

      running(app) {

        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.get(vrn).futureValue

        result mustBe Left(ServerError)
      }
    }

    "must return Left(InvalidJson) when the server returns OK with a payload that cannot be parsed" in {

      val app = application

      val responseJson = """{ "foo": "bar" }"""

      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(ok(responseJson))
      )

      running(app) {

        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.get(vrn).futureValue

        result mustBe Left(InvalidJson)
      }
    }

    "must return Left(UnexpectedResponse) when the server returns an unexpected response code" in {

      val app = application

      val status = Gen.oneOf(401, 402, 403, 501, 502).sample.value

      val errorResponseJson =
        s"""{
           |  "error": "$status",
           |  "errorMessage": "Error"
           |}""".stripMargin

      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(aResponse()
            .withStatus(status)
            .withBody(errorResponseJson))
      )

      running(app) {

        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.get(vrn).futureValue

        result mustBe Left(UnexpectedResponseStatus(status, s"Unexpected response from DES, received status $status with body $errorResponseJson"))
      }
    }

    "must return Left(GatewayTimeout) when the server returns a GatewayTimeoutException" in {

      val app = application

      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(aResponse()
            .withStatus(504)
            .withFixedDelay(21000))
      )

      running(app) {

        val connector = app.injector.instanceOf[RegistrationConnector]
        whenReady(connector.get(vrn), Timeout(Span(30, Seconds))) { exp =>
          exp mustBe Left(GatewayTimeout)
        }
      }
    }

  }

}