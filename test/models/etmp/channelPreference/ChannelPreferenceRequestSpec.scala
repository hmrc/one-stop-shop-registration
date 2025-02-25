package models.etmp.channelPreference

import base.BaseSpec
import play.api.libs.json.{JsSuccess, Json}

class ChannelPreferenceRequestSpec extends BaseSpec {

  private val channelPreferenceRequest: ChannelPreferenceRequest = arbitraryChannelPreferenceRequest.arbitrary.sample.value

  "ChannelPreferenceRequest" - {

    "must deserialise/serialise to and from ChannelPreferenceRequest" in {

      val json = Json.obj(
        "identifierType" -> channelPreferenceRequest.identifierType,
        "identifier" -> channelPreferenceRequest.identifier,
        "emailAddress" -> channelPreferenceRequest.emailAddress,
        "unusableStatus" -> channelPreferenceRequest.unusableStatus
      )

      val expectedResult = ChannelPreferenceRequest(
        identifierType = channelPreferenceRequest.identifierType,
        identifier = channelPreferenceRequest.identifier,
        emailAddress = channelPreferenceRequest.emailAddress,
        unusableStatus = channelPreferenceRequest.unusableStatus
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[ChannelPreferenceRequest] mustBe JsSuccess(expectedResult)
    }
  }
}
