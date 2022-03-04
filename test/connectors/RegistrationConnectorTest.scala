package connectors

import base.BaseSpec
import com.github.tomakehurst.wiremock.client.WireMock._
import generators.Generators
import models.`if`.IfErrorResponse
import models.requests.RegistrationRequest
import models.{BankDetails, ContactDetails, Registration, VatDetails}
import org.scalacheck.Arbitrary.arbitrary
import play.api.Application
import play.api.http.HeaderNames._
import play.api.http.MimeTypes
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.domain.Vrn

import java.time.{Instant, LocalDate}

class RegistrationConnectorTest extends BaseSpec with WireMockHelper  with Generators {

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.if.host" -> "127.0.0.1",
        "microservice.services.if.port" -> server.port,
        "microservice.services.if.authorizationToken" -> "auth-token",
        "microservice.services.if.environment" -> "test-environment"
      )
      .build()

  def getRegistrationUrl(vrn: Vrn) = s"/one-stop-shop-registration-stub/getRegistration/${vrn.value}"

  def createRegistrationUrl = "/one-stop-shop-registration-stub/createRegistration"

  def generateRegistration(vrn: Vrn) = Registration(
    vrn                   = vrn,
    registeredCompanyName = arbitrary[String].sample.value,
    tradingNames          = Seq.empty,
    vatDetails            = arbitrary[VatDetails].sample.value,
    euRegistrations       = Seq.empty,
    contactDetails        = arbitrary[ContactDetails].sample.value,
    websites              = Seq.empty,
    commencementDate      = LocalDate.now,
    previousRegistrations = Seq.empty,
    bankDetails           = arbitrary[BankDetails].sample.value,
    isOnlineMarketplace   = arbitrary[Boolean].sample.value,
    niPresence            = None,
    dateOfFirstSale       = None,
    submissionReceived    = Instant.now(stubClock),
    lastUpdated           = Instant.now(stubClock)
  )

  def toRegistrationRequest(registration: Registration) = {
    RegistrationRequest(
      registration.vrn,
      registration.registeredCompanyName,
      registration.tradingNames,
      registration.vatDetails,
      registration.euRegistrations,
      registration.contactDetails,
      registration.websites,
      registration.commencementDate,
      registration.previousRegistrations,
      registration.bankDetails,
      registration.isOnlineMarketplace,
      registration.niPresence,
      registration.dateOfFirstSale
    )
  }

  "create" - {

    "should return Registration payload correctly" in {

      val app = application

      val registration = generateRegistration(vrn)

      val registrationRequest = toRegistrationRequest(registration)

      val requestJson = Json.stringify(Json.toJson(registrationRequest))

      server.stubFor(
        post(urlEqualTo(createRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse().withStatus(ACCEPTED))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.create(registrationRequest).futureValue
        result mustEqual Right((): Unit)
      }
    }

    "should return errors correctly" in {

      val app = application

      val registration = generateRegistration(vrn)

      val registrationRequest = toRegistrationRequest(registration)

      val requestJson = Json.stringify(Json.toJson(registrationRequest))

      server.stubFor(
        post(urlEqualTo(createRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse().withStatus(CONFLICT))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.create(registrationRequest).futureValue
        result mustEqual Left(IfErrorResponse(CONFLICT, ""))
      }
    }
  }

  "get" - {

    "Should parse Registration payload correctly" in {

      val app = application

      val registration = generateRegistration(vrn)

      val responseJson = Json.prettyPrint(Json.toJson(registration))

      server.stubFor(
        get(urlEqualTo(getRegistrationUrl(vrn)))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .willReturn(ok(responseJson))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.get(vrn).futureValue

        val expectedResult = registration
        result mustEqual expectedResult
      }
    }
  }
}
