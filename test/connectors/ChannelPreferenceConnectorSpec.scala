package connectors

import base.BaseSpec
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, put, urlEqualTo}
import models.etmp.channelPreference.ChannelPreferenceRequest
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, OK, UNAUTHORIZED}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier

class ChannelPreferenceConnectorSpec extends BaseSpec with WireMockHelper {

  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  private val basePath = "channel-preference/"
  private val channelPreferenceRequest: ChannelPreferenceRequest = arbitraryChannelPreferenceRequest.arbitrary.sample.value

  "ChannelPreferenceConnector" - {

    ".updatePreferences" - {

      def application: Application = new GuiceApplicationBuilder()
        .configure(
          "microservice.services.channel-preference.host" -> "127.0.0.1",
          "microservice.services.channel-preference.port" -> server.port,
          "microservice.services.channel-preference.authorizationToken" -> "auth-token",
          "microservice.services.channel-preference.basePath" -> basePath
        ).build()

      val url: String = s"/${basePath}income-tax/customer/OSS/contact-preference"

      "must return an HttpResponse with status OK when the server returns NoContent" in {

        val app = application

        server.stubFor(
          put(urlEqualTo(url))
            .willReturn(aResponse()
              .withStatus(OK))
        )

        running(app) {
          val connector = app.injector.instanceOf[ChannelPreferenceConnector]

          val result = connector.updatePreferences(channelPreferenceRequest).futureValue

          result.status mustBe OK
        }
      }

      Seq(BAD_REQUEST, UNAUTHORIZED).foreach { status =>
        s"must return an Http response with $status when the server returns $status" in {

          val app = application

          server.stubFor(
            put(urlEqualTo(url))
              .willReturn(aResponse()
                .withStatus(status))
          )

          running(app) {
            val connector = app.injector.instanceOf[ChannelPreferenceConnector]

            val result = connector.updatePreferences(channelPreferenceRequest).futureValue

            result.status mustBe status
          }
        }
      }
    }
  }
}
