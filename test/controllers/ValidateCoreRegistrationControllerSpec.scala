package controllers

import base.BaseSpec
import connectors.ValidateCoreRegistrationConnector
import models.core.{CoreRegistrationRequest, CoreRegistrationValidationResult, Match, SourceType, TraderId}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import java.time.LocalDate
import scala.concurrent.Future

class ValidateCoreRegistrationControllerSpec extends BaseSpec {

  "post" - {

    val coreValidationRequest = CoreRegistrationRequest(SourceType.VATNumber.toString, None, vrn.vrn, None, "GB")

    val coreRegistrationValidationResult: CoreRegistrationValidationResult =
      CoreRegistrationValidationResult(
        "IM2344433220",
        Some("IN4747493822"),
        "FR",
        true,
        Seq(Match(
          TraderId("IM0987654321"),
          Some("444444444"),
          "DE",
          Some(3),
          Some(LocalDate.now().format(Match.dateFormatter)),
          Some(LocalDate.now().format(Match.dateFormatter)),
          Some(1),
          Some(2)
        ))
      )
    "must return 200 when returning a match" in {

      val mockConnector = mock[ValidateCoreRegistrationConnector]
      when(mockConnector.validateCoreRegistration(any())) thenReturn Future.successful(Right(coreRegistrationValidationResult))

      val app =
        applicationBuilder
          .overrides(bind[ValidateCoreRegistrationConnector].toInstance(mockConnector))
          .build()

      running(app) {

        val request =
          FakeRequest(POST, routes.ValidateCoreRegistrationController.post().url)
            .withJsonBody(Json.toJson(coreValidationRequest))

        val result = route(app, request).value

        status(result) mustEqual OK
      }
    }
/*
    "must return 400 when the JSON request payload is not a registration" in {

      val app = applicationBuilder.build()

      running(app) {

        val request =
          FakeRequest(POST, routes.RegistrationController.create().url)
            .withJsonBody(Json.toJson(RegistrationData.invalidRegistration))

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }*/

  }
}
