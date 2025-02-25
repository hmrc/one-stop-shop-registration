package models.external

import base.BaseSpec
import play.api.libs.json.{JsSuccess, Json}

class EventSpec extends BaseSpec {

  private val event: Event = arbitraryEvent.arbitrary.sample.value

  "Event" - {

    "must deserialise/serialise to and from Event" in {

      val json = Json.obj(
        "eventId" -> event.eventId,
        "subject" -> event.subject,
        "groupId" -> event.groupId,
        "event" -> event.event
      )

      val expectedResult = Event(
        eventId = event.eventId,
        subject = event.subject,
        groupId = event.groupId,
        event = event.event
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[Event] mustBe JsSuccess(expectedResult)
    }
  }
}

