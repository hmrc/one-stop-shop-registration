package connectors

import base.BaseSpec
import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.RegistrationHttpParser.serviceName
import generators.Generators
import models._
import models.enrolments.{EtmpEnrolmentErrorResponse, EtmpEnrolmentResponse, EtmpErrorDetail}
import models.requests.RegistrationRequest
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import play.api.Application
import play.api.http.HeaderNames._
import play.api.http.MimeTypes
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.domain.Vrn

import java.time.{Instant, LocalDate, LocalDateTime}

class RegistrationConnectorSpec extends BaseSpec with WireMockHelper  with Generators {

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

  def createRegistrationUrl = "/one-stop-shop-registration-stub/vec/ossregistration/regdatatransfer/v1"

  def getValidateRegistrationUrl(vrn: Vrn) = s"/one-stop-shop-registration-stub/validateRegistration/${vrn.value}"


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
    lastUpdated           = Instant.now(stubClock),
    nonCompliantReturns   = Some(1),
    nonCompliantPayments  = Some(2)
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
      registration.dateOfFirstSale,
      registration.nonCompliantReturns,
      registration.nonCompliantPayments
    )
  }


  ".create" - {

    Seq(CREATED).foreach {
      status =>
        s"when status is $status" - {
          "should return Registration payload correctly" in {

            val now = LocalDateTime.now()

            val formBundleNumber = "123456789"

            val app = application

            val requestJson = Json.stringify(Json.toJson(etmpRegistrationRequest))

            server.stubFor(
              post(urlEqualTo(createRegistrationUrl))
                .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
                .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
                .withRequestBody(equalTo(requestJson))
                .willReturn(aResponse().withStatus(status)
                  .withBody(Json.stringify(Json.toJson(EtmpEnrolmentResponse(now, vrn.vrn, formBundleNumber)))))
            )

            running(app) {
              val connector = app.injector.instanceOf[RegistrationConnector]
              val result = connector.create(etmpRegistrationRequest).futureValue
              result mustBe Right(EtmpEnrolmentResponse(now, vrn.vrn, formBundleNumber))
            }
          }

          "should return Invalid Json when server responds with InvalidJson" in {

            val app = application

            val requestJson = Json.stringify(Json.toJson(etmpRegistrationRequest))

            server.stubFor(
              post(urlEqualTo(createRegistrationUrl))
                .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
                .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
                .withRequestBody(equalTo(requestJson))
                .willReturn(aResponse()
                  .withStatus(status)
                  .withBody(Json.stringify(Json.toJson("tests" -> "invalid"))))
            )

            running(app) {
              val connector = app.injector.instanceOf[RegistrationConnector]
              val result = connector.create(etmpRegistrationRequest).futureValue
              result mustBe Left(InvalidJson)
            }
          }
        }

    }

    "should return EtmpError when server responds with status 422 and correct error response json" in {

      val app = application

      val requestJson = Json.stringify(Json.toJson(etmpRegistrationRequest))

      val errorResponse = EtmpEnrolmentErrorResponse(EtmpErrorDetail(LocalDate.now(stubClock), "correlation-id1", "123", "error", "source"))

      server.stubFor(
        post(urlEqualTo(createRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse().withStatus(UNPROCESSABLE_ENTITY)
            .withBody(Json.stringify(Json.toJson(errorResponse))))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.create(etmpRegistrationRequest).futureValue
        result mustBe Left(EtmpEnrolmentError("123", "error"))
      }
    }

    "should return Invalid Json when server responds with status 422 and incorrect error response json" in {

      val app = application

      val requestJson = Json.stringify(Json.toJson(etmpRegistrationRequest))

      server.stubFor(
        post(urlEqualTo(createRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse().withStatus(UNPROCESSABLE_ENTITY)
            .withBody(Json.stringify(Json.toJson("tests" -> "invalid"))))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.create(etmpRegistrationRequest).futureValue
        result mustBe Left(UnexpectedResponseStatus(UNPROCESSABLE_ENTITY, "Unexpected response from etmp registration, received status 422"))
      }
    }


    Seq((NOT_FOUND, NotFound), (CONFLICT, Conflict), (INTERNAL_SERVER_ERROR, ServerError), (BAD_REQUEST, InvalidVrn), (SERVICE_UNAVAILABLE, ServiceUnavailable), (123, UnexpectedResponseStatus(123, s"Unexpected response from ${serviceName}, received status 123")))
      .foreach { error =>
        s"should return correct error response when server responds with ${error._1}" in {

          val app = application

          val requestJson = Json.stringify(Json.toJson(etmpRegistrationRequest))

          server.stubFor(
            post(urlEqualTo(createRegistrationUrl))
              .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
              .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
              .withRequestBody(equalTo(requestJson))
              .willReturn(aResponse().withStatus(error._1))
          )

          running(app) {
            val connector = app.injector.instanceOf[RegistrationConnector]
            val result = connector.create(etmpRegistrationRequest).futureValue
            result mustBe Left(UnexpectedResponseStatus(error._1, s"Unexpected response from etmp registration, received status ${error._1}"))
          }
        }
      }

    "should return Error Response when server responds with Http Exception" in {

      val app = application

      val requestJson = Json.stringify(Json.toJson(etmpRegistrationRequest))

      server.stubFor(
        post(urlEqualTo(createRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse()
            .withStatus(504)
            .withFixedDelay(21000))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        whenReady(connector.create(etmpRegistrationRequest), Timeout(Span(30, Seconds))) { exp =>
          exp.isLeft mustBe true
          exp.left.get mustBe a[ErrorResponse]
        }
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
        result mustBe Right(expectedResult)

      }
    }

    Seq((NOT_FOUND, NotFound), (CONFLICT, Conflict), (INTERNAL_SERVER_ERROR, ServerError), (BAD_REQUEST, InvalidVrn), (SERVICE_UNAVAILABLE, ServiceUnavailable), (123, UnexpectedResponseStatus(123, s"Unexpected response from ${serviceName}, received status 123")))
      .foreach { error =>
        s"should return correct error response when server responds with ${error._1}" in {

          val app = application

          server.stubFor(
            get(urlEqualTo(getRegistrationUrl(vrn)))
              .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
              .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
              .willReturn(aResponse().withStatus(error._1))
          )

          running(app) {
            val connector = app.injector.instanceOf[RegistrationConnector]
            val result = connector.get(vrn).futureValue
            result mustBe Left(error._2)
          }
        }
      }

    "should return Error Response when server responds with Http Exception" in {

      val app = application

      server.stubFor(
        get(urlEqualTo(getRegistrationUrl(vrn)))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .willReturn(aResponse()
            .withStatus(504)
            .withFixedDelay(21000))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        whenReady(connector.get(vrn), Timeout(Span(30, Seconds))) { exp =>
          exp.isLeft mustBe true
          exp.left.get mustBe a[ErrorResponse]
        }
      }
    }
  }

}
