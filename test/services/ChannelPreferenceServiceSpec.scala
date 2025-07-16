package services

import base.BaseSpec
import connectors.ChannelPreferenceConnector
import models.etmp.channelPreference.ChannelPreferenceRequest
import models.external.Event
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.FutureSyntax.FutureOps

import scala.concurrent.ExecutionContext.Implicits.global

class ChannelPreferenceServiceSpec extends BaseSpec with BeforeAndAfterEach {

  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  private val mockChannelPreferenceConnector: ChannelPreferenceConnector = mock[ChannelPreferenceConnector]

  private val event: Event = arbitraryEvent.arbitrary.sample.value
  private val enrolmentString: String = s"HMRC-OSS-ORG~VRN~$vrn"

  override def beforeEach(): Unit = {
    Mockito.reset(mockChannelPreferenceConnector)
    super.beforeEach()
  }

  "ChannelPreferenceService" - {

    ".updatePreferences" - {

      "must call channel preference with correct VRN and reply when successful" in {

        val updatedEvent = event.copy(event = event.event.copy(tags = Map("enrolment" -> enrolmentString)))
        val httpResponse = HttpResponse(OK, "")

        when(mockChannelPreferenceConnector.updatePreferences(any())(any())) thenReturn httpResponse.toFuture

        val service = new ChannelPreferenceService(mockChannelPreferenceConnector)

        val result = service.updatePreferences(updatedEvent).futureValue

        val expectedRequest = ChannelPreferenceRequest("VRN", vrn.toString, event.event.emailAddress, unusableStatus = true)

        result `mustBe` true
        
        verify(mockChannelPreferenceConnector, times(1)).updatePreferences(eqTo(expectedRequest))(any())
      }

      "must call channel preference with correct VRN with spaces and reply when successful" in {

        val enrolmentStringWithSpaces = s"HMRC-OSS-ORG~VRN~123 4567 89"

        val updatedEvent = event.copy(event = event.event.copy(tags = Map("enrolment" -> enrolmentStringWithSpaces)))
        val httpResponse = HttpResponse(OK, "")

        when(mockChannelPreferenceConnector.updatePreferences(any())(any())) thenReturn httpResponse.toFuture

        val service = new ChannelPreferenceService(mockChannelPreferenceConnector)

        val result = service.updatePreferences(updatedEvent).futureValue

        val expectedRequest = ChannelPreferenceRequest("VRN", vrn.toString, event.event.emailAddress, unusableStatus = true)

        result `mustBe` true
        
        verify(mockChannelPreferenceConnector, times(1)).updatePreferences(eqTo(expectedRequest))(any())
      }

      "must reply with false when failed" in {

        val updatedEvent = event.copy(event = event.event.copy(tags = Map("enrolment" -> enrolmentString)))
        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, "")

        when(mockChannelPreferenceConnector.updatePreferences(any())(any())) thenReturn httpResponse.toFuture

        val service = new ChannelPreferenceService(mockChannelPreferenceConnector)

        val result = service.updatePreferences(updatedEvent).futureValue

        val expectedRequest = ChannelPreferenceRequest("VRN", vrn.toString, event.event.emailAddress, unusableStatus = true)

        result `mustBe` false
        
        verify(mockChannelPreferenceConnector, times(1)).updatePreferences(eqTo(expectedRequest))(any())
      }

      "must throw an Illegal State Exception when enrolment can't be determined" in {

        val httpResponse = HttpResponse(OK, "")

        when(mockChannelPreferenceConnector.updatePreferences(any())(any())) thenReturn httpResponse.toFuture

        val service = new ChannelPreferenceService(mockChannelPreferenceConnector)

        val exception = intercept[IllegalStateException](service.updatePreferences(event))

        exception.getMessage mustBe s"Unable to get enrolment from event with tags ${event.event.tags}"
      }
    }
  }
}
