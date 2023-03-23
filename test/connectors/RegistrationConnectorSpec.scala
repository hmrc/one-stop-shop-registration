package connectors

import base.BaseSpec
import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.RegistrationHttpParser.serviceName
import generators.Generators
import models._
import models.enrolments.{EtmpEnrolmentErrorResponse, EtmpEnrolmentResponse, EtmpErrorDetail}
import models.etmp.AmendRegistrationResponse
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
import utils.DisplayRegistrationData.{arbitraryDisplayRegistration, writesEtmpSchemeDetails}
import testutils.RegistrationData.optionalDisplayRegistration
import uk.gov.hmrc.domain.Vrn

import java.time.{Instant, LocalDate, LocalDateTime}

class RegistrationConnectorSpec extends BaseSpec with WireMockHelper with Generators {

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.if.host" -> "127.0.0.1",
        "microservice.services.if.port" -> server.port,
        "microservice.services.if.authorizationToken" -> "auth-token",
        "microservice.services.if.environment" -> "test-environment",
        "microservice.services.display-registration.host" -> "127.0.0.1",
        "microservice.services.display-registration.port" -> server.port,
        "microservice.services.display-registration.authorizationToken" -> "auth-token",
        "microservice.services.display-registration.environment" -> "test-environment",
        "microservice.services.amend-registration.host" -> "127.0.0.1",
        "microservice.services.amend-registration.port" -> server.port
      )
      .build()

  def getDisplayRegistrationUrl(vrn: Vrn) = s"/one-stop-shop-registration-stub/RESTAdapter/OSS/Subscription/${vrn.value}"

  def createRegistrationUrl = "/one-stop-shop-registration-stub/vec/ossregistration/regdatatransfer/v1"

  def getValidateRegistrationUrl(vrn: Vrn) = s"/one-stop-shop-registration-stub/validateRegistration/${vrn.value}"

  def getAmendRegistrationUrl = "/one-stop-shop-registration-stub/RESTAdapter/OSS/Subscription/"


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
    submissionReceived    = Some(Instant.now(stubClock)),
    lastUpdated           = Some(Instant.now(stubClock)),
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

  private val amendRegistrationResponse: AmendRegistrationResponse =
    AmendRegistrationResponse(
      processingDateTime = LocalDateTime.now(),
      formBundleNumber = "12345",
      vrn = "123456789",
      businessPartner = "businessPartner"
    )


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
          exp.left.toOption.get mustBe a[ErrorResponse]
        }
      }
    }
  }

  "get" - {

    "Should parse Registration payload with all optional fields present correctly" in {

      val app = application

      val etmpRegistration = arbitraryDisplayRegistration

      val responseJson =
        s"""{
          | "tradingNames" : ${Json.toJson(etmpRegistration.tradingNames)},
          | "schemeDetails" :${Json.toJson(etmpRegistration.schemeDetails)(writesEtmpSchemeDetails)},
          | "bankDetails" : ${Json.toJson(etmpRegistration.bankDetails)}
          |}""".stripMargin

      server.stubFor(
        get(urlEqualTo(getDisplayRegistrationUrl(vrn)))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .willReturn(aResponse().withStatus(OK)
            .withBody(responseJson))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.get(vrn).futureValue
        val expectedResult = etmpRegistration
        result mustBe Right(expectedResult)

      }
    }

    "Should parse Registration payload without all optional fields present correctly" in {

      val app = application

      val etmpRegistration = optionalDisplayRegistration

      val responseJson =
        """{
          | "tradingNames" : [],
          | "schemeDetails" : {
          |   "commencementDate" : "2023-01-01",
          |   "euRegistrationDetails" : [],
          |   "previousEURegistrationDetails" : [],
          |   "onlineMarketPlace" : true,
          |   "websites" : [],
          |   "contactDetails" : {
          |     "contactNameOrBusinessAddress" : "Mr Test",
          |     "businessTelephoneNumber" : "1234567890",
          |     "businessEmailAddress" : "test@testEmail.com"
          |   }
          | },
          | "bankDetails" : {
          |   "accountName" : "Bank Account Name",
          |   "iban" : "GB33BUKB20201555555555"
          | }
          |}""".stripMargin

      server.stubFor(
        get(urlEqualTo(getDisplayRegistrationUrl(vrn)))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .willReturn(aResponse().withStatus(OK)
            .withBody(responseJson))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.get(vrn).futureValue
        val expectedResult = etmpRegistration
        result mustBe Right(expectedResult)

      }
    }

    "must return Left(InvalidJson) when the server returns OK with a payload that cannot be parsed" in {

      val app = application

      val responseJson = """{ "foo": "bar" }"""

      server.stubFor(
        get(urlEqualTo(getDisplayRegistrationUrl(vrn)))
          .willReturn(ok(responseJson))
      )

      running(app) {

        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.get(vrn).futureValue

        result mustBe Left(InvalidJson)
      }
    }


    val body = ""

    Seq((NOT_FOUND, NotFound), (CONFLICT, Conflict), (INTERNAL_SERVER_ERROR, ServerError), (BAD_REQUEST, InvalidVrn), (SERVICE_UNAVAILABLE, ServiceUnavailable), (123, UnexpectedResponseStatus(123, s"Unexpected response from ${serviceName}, received status 123 with body $body")))
      .foreach { error =>
        s"should return correct error response when server responds with ${error._1}" in {

          val app = application

          server.stubFor(
            get(urlEqualTo(getDisplayRegistrationUrl(vrn)))
              .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
              .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
              .willReturn(aResponse().withStatus(error._1).withBody(body))
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
        get(urlEqualTo(getDisplayRegistrationUrl(vrn)))
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
          exp.left.toOption.get mustBe a[ErrorResponse]
        }
      }
    }
  }

  ".amendRegistration" - {

    "must return Ok with an Amend Registration response when a valid payload is sent" in {

      val app = application

      val requestJson = Json.stringify(Json.toJson(etmpRegistrationRequest))

      server.stubFor(
        put(urlEqualTo(getAmendRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse()
            .withStatus(OK)
            .withBody(Json.stringify(Json.toJson(amendRegistrationResponse))))
      )

      running(app) {

        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.amendRegistration(etmpRegistrationRequest).futureValue

        result mustBe Right(amendRegistrationResponse)
      }

    }

    "must return not found when server responds with NOT_FOUND" in {

      val app = application

      val requestJson = Json.stringify(Json.toJson(etmpRegistrationRequest))

      server.stubFor(
        put(urlEqualTo(getAmendRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse()
            .withStatus(NOT_FOUND))
      )

      running(app) {

        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.amendRegistration(etmpRegistrationRequest).futureValue

        result mustBe Left(NotFound)
      }

    }

    "must return unprocessable entity when server responds with UNPROCESSABLE_ENTITY" in {

      val app = application

      val requestJson = Json.stringify(Json.toJson(etmpRegistrationRequest))

      server.stubFor(
        put(urlEqualTo(getAmendRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse()
            .withStatus(UNPROCESSABLE_ENTITY))
      )

      running(app) {

        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.amendRegistration(etmpRegistrationRequest).futureValue

        result mustBe Left(NotFound)
      }

    }
  }

}
