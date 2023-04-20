package models.audit

import base.BaseSpec
import controllers.actions.AuthorisedMandatoryVrnRequest
import generators.Generators
import models.enrolments.EtmpEnrolmentResponse
import models.ServerError
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest

import java.time.LocalDateTime

class EtmpRegistrationAuditModelSpec extends BaseSpec with Matchers with ScalaCheckPropertyChecks with Generators {

  implicit private lazy val request: AuthorisedMandatoryVrnRequest[AnyContent] = AuthorisedMandatoryVrnRequest(FakeRequest(), userId, vrn)

  "EtmpRegistrationAuditModel" - {

    "must create correct json object" - {

      "when result is success" in {

        val etmpEnrolmentResponse = EtmpEnrolmentResponse(LocalDateTime.now(), vrn.vrn, "123456789")
        val etmpRegistrationAuditModel = EtmpRegistrationAuditModel.build(etmpRegistrationRequest,
          Some(etmpEnrolmentResponse), None, SubmissionResult.Success)

        val expectedJson = Json.obj(
          "userId" -> request.userId,
          "browserUserAgent" -> "",
          "requestersVrn" -> request.vrn.vrn,
          "etmpRegistrationRequest" -> etmpRegistrationRequest,
          "etmpEnrolmentResponse" -> etmpEnrolmentResponse,
          "submissionResult" -> SubmissionResult.Success.toString
        )
        etmpRegistrationAuditModel.detail mustEqual expectedJson
      }

      "when result is conflict" in {

        val etmpRegistrationAuditModel = EtmpRegistrationAuditModel.build(etmpRegistrationRequest,
          None, None, SubmissionResult.Duplicate)

        val expectedJson = Json.obj(
          "userId" -> request.userId,
          "browserUserAgent" -> "",
          "requestersVrn" -> request.vrn.vrn,
          "etmpRegistrationRequest" -> etmpRegistrationRequest,
          "submissionResult" -> SubmissionResult.Duplicate.toString
        )
        etmpRegistrationAuditModel.detail mustEqual expectedJson
      }

      "when result is error" in {

        val etmpRegistrationAuditModel = EtmpRegistrationAuditModel.build(etmpRegistrationRequest,
          None, Some(ServerError.body), SubmissionResult.Failure)

        val expectedJson = Json.obj(
          "userId" -> request.userId,
          "browserUserAgent" -> "",
          "requestersVrn" -> request.vrn.vrn,
          "etmpRegistrationRequest" -> etmpRegistrationRequest,
          "submissionResult" -> SubmissionResult.Failure.toString,
          "errorResponse" -> ServerError.body
        )
        etmpRegistrationAuditModel.detail mustEqual expectedJson
      }
    }
  }

}
