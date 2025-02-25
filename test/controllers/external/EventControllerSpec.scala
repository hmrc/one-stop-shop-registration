package controllers.external

import base.BaseSpec
import models.external.Event
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ChannelPreferenceService
import utils.FutureSyntax.FutureOps

class EventControllerSpec extends BaseSpec with BeforeAndAfterEach {

  private val mockChannelPreferenceService: ChannelPreferenceService = mock[ChannelPreferenceService]

  private val event: Event = arbitraryEvent.arbitrary.sample.value

  override def beforeEach(): Unit = {
    Mockito.reset(mockChannelPreferenceService)
    super.beforeEach()
  }

  "EventController" - {

    "must accept an event and reply NoContent when successful" in {

      when(mockChannelPreferenceService.updatePreferences(any())(any())) thenReturn true.toFuture

      val application = applicationBuilder
        .overrides(bind[ChannelPreferenceService].toInstance(mockChannelPreferenceService))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.EventController.processBouncedEmailEvent().url)
          .withJsonBody(
            Json.toJson(event)
          )

        val result = route(application, request).value

        status(result) `mustBe` NO_CONTENT

        verify(mockChannelPreferenceService, times(1)).updatePreferences(eqTo(event))(any())
      }
    }

    "must reply InternalServerError when there's an error from the downstream" in {

      when(mockChannelPreferenceService.updatePreferences(any())(any())) thenReturn false.toFuture

      val application = applicationBuilder
        .overrides(bind[ChannelPreferenceService].toInstance(mockChannelPreferenceService))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.EventController.processBouncedEmailEvent().url)
          .withJsonBody(
            Json.toJson(event)
          )

        val result = route(application, request).value

        status(result) `mustBe` INTERNAL_SERVER_ERROR

        verify(mockChannelPreferenceService, times(1)).updatePreferences(eqTo(event))(any())
      }
    }

    "must reply BadRequest when there is a payload error" in {

      val invalidJsonPayload: String = """{"invalidJson": ""}"""

      when(mockChannelPreferenceService.updatePreferences(any())(any())) thenReturn true.toFuture

      val application = applicationBuilder
        .overrides(bind[ChannelPreferenceService].toInstance(mockChannelPreferenceService))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.EventController.processBouncedEmailEvent().url)
          .withJsonBody(
            Json.toJson(invalidJsonPayload)
          )

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST

        verifyNoInteractions(mockChannelPreferenceService)
      }
    }
  }
}
