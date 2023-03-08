package connectors

import base.BaseSpec
import com.github.tomakehurst.wiremock.client.WireMock._
import models.NotFound
import models.etmp._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
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

    "must return Right(EtmpSchemeDetails) when server returns OK with a recognised payload" in {

      val app = application

      val responseJson =
        """{
          | "commencementDate" : "2000-01-01",
          | "firstSaleDate" : "2000-01-01",
          | "euRegistrationDetails" : [ {
          |     "countryOfRegistration" : "FR",
          |     "taxIdentificationNumber" : "FR123456789",
          |     "fixedEstablishment" : true,
          |     "tradingName" : "The French Company",
          |     "fixedEstablishmentAddressLine1" : "Line1",
          |     "fixedEstablishmentAddressLine2" : "Line2",
          |     "townOrCity" : "Town",
          |     "regionOrState" : "Region",
          |     "postcode" : "Postcode"
          |   },
          |   {
          |     "countryOfRegistration" : "BE"
          |   }],
          | "previousEURegistrationDetails" : [{
          |   "issuedBy" : "DE",
          |   "registrationNumber" : "DE123456789",
          |   "schemeType" : "IOSSWithIntermediary",
          |   "intermediaryNumber" : "IM1234567"
          | }],
          | "onlineMarketPlace" : false,
          | "websites" : [ {
          |   "websiteAddress" : "website1"
          |   },
          |   {
          |   "websiteAddress" : "website2"
          |  }],
          | "contactName" : "Joe Bloggs",
          | "businessTelephoneNumber" : "01112223344",
          | "businessEmailId" : "email@email.com",
          | "nonCompliantReturns" : 1,
          | "nonCompliantPayments" : 2
          |}""".stripMargin

      server.stubFor(
        get(urlEqualTo(url))
          .withHeader("Authorization", equalTo("Bearer auth-token"))
          .withHeader("Environment", equalTo("test-environment"))
          .willReturn(ok(responseJson))
      )

      running(app) {

        val connector = application.injector.instanceOf[DisplayRegistrationConnector]

        val result = connector.displayRegistration(vrn).futureValue

        val expectedResult = EtmpSchemeDetails(
          commencementDate = LocalDate.of(2000, 1, 1).format(dateFormatter),
          firstSaleDate = Some(LocalDate.of(2000, 1, 1).format(dateFormatter)),
          euRegistrationDetails = Seq(
            EtmpEuRegistrationDetails(
              countryOfRegistration = "FR",
              taxIdentificationNumber = Some("FR123456789"),
              fixedEstablishment = Some(true),
              tradingName = Some("The French Company"),
              fixedEstablishmentAddressLine1 = Some("Line1"),
              fixedEstablishmentAddressLine2 = Some("Line2"),
              townOrCity = Some("Town"),
              regionOrState = Some("Region"),
              postcode = Some("Postcode")
            ),
            EtmpEuRegistrationDetails(
              countryOfRegistration = "BE"
            )
          ),
          previousEURegistrationDetails = Seq(
            EtmpPreviousEURegistrationDetails(
              issuedBy = "DE",
              registrationNumber = "DE123456789",
              schemeType = SchemeType.IOSSWithIntermediary,
              intermediaryNumber = Some("IM1234567")
            )
          ),
          onlineMarketPlace = false,
          websites = Seq(
            Website("website1"),
            Website("website2")
          ),
          contactName = "Joe Bloggs",
          businessTelephoneNumber = "01112223344",
          businessEmailId = "email@email.com",
          nonCompliantReturns = Some(1),
          nonCompliantPayments = Some(2)
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

        val connector = application.injector.instanceOf[DisplayRegistrationConnector]

        val result = connector.displayRegistration(vrn).futureValue

        result mustBe Left(NotFound)
      }
    }

  }

}
