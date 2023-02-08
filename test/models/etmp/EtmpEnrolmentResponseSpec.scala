package models.etmp

import base.BaseSpec
import models._
import models.EuTaxIdentifierType.Vat
import models.VatDetailSource.UserEntered
import models.enrolments.EtmpEnrolmentResponse
import models.requests.RegistrationRequest
import play.api.libs.json.{Json, JsSuccess}
import uk.gov.hmrc.domain.Vrn

import java.time.{LocalDate, LocalDateTime}

class EtmpEnrolmentResponseSpec extends BaseSpec {

  "EtmpEnrolmentResponse" - {

    "parse json" in {

      val json = """{"processingDateTime":"2023-02-07T12:42:19Z","formBundleNumber":"190000000280","vrn":"110211108","businessPartner":"0100400987"}"""

      val test = Json.parse(json).validate[EtmpEnrolmentResponse]

      test mustBe JsSuccess(EtmpEnrolmentResponse(LocalDateTime.parse("2023-02-07T12:42:19"), "110211108", "190000000280"))
    }
  }

}


