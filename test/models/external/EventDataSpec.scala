package models.external

import base.BaseSpec
import play.api.libs.json.{JsSuccess, Json}

class EventDataSpec extends BaseSpec {

  private val eventData: EventData = arbitraryEventData.arbitrary.sample.value

  "EventData" - {

    "must deserialise/serialise to and from EventData" in {

      val json = Json.obj(
        "emailAddress" -> eventData.emailAddress,
        "tags" -> eventData.tags
      )

      val expectedResult = EventData(
        emailAddress = eventData.emailAddress,
        tags = eventData.tags
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EventData] mustBe JsSuccess(expectedResult)
    }
  }
}
