package models.etmp

import base.BaseSpec
import models.enrolments.EtmpEnrolmentResponse
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDateTime

class EtmpEnrolmentResponseSpec extends BaseSpec {

  "EtmpEnrolmentResponse" - {

    "parse json" in {

      val json = """{"processingDateTime":"2023-02-07T12:42:19Z","formBundleNumber":"190000000280","vrn":"110211108","businessPartner":"0100400987"}"""

      val test = Json.parse(json).validate[EtmpEnrolmentResponse]

      test mustBe JsSuccess(EtmpEnrolmentResponse(LocalDateTime.parse("2023-02-07T12:42:19"), "110211108", "190000000280"))
    }
  }

}


