/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import base.BaseSpec
import models.repository.AmendResult.AmendSucceeded
import models.repository.InsertResult.{AlreadyExists, InsertSucceeded}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import play.api.http.Status.CREATED
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{RegistrationService, RegistrationServiceEtmpImpl}
import testutils.RegistrationData

import scala.concurrent.Future


class RegistrationControllerSpec extends BaseSpec {

  "create" - {

    "must return 201 when given a valid payload and the registration is created successfully" in {

      val mockService = mock[RegistrationServiceEtmpImpl]
      when(mockService.createRegistration(any())(any(), any())) thenReturn Future.successful(InsertSucceeded)

      val app =
        applicationBuilder
          .overrides(bind[RegistrationService].toInstance(mockService))
          .build()

      running(app) {

        val request =
          FakeRequest(POST, routes.RegistrationController.create().url)
            .withJsonBody(Json.toJson(RegistrationData.registration))

        val result = route(app, request).value

        status(result) mustEqual CREATED
      }
    }

    "must return 400 when the JSON request payload is not a registration" in {

      val app = applicationBuilder.build()

      running(app) {

        val request =
          FakeRequest(POST, routes.RegistrationController.create().url)
            .withJsonBody(Json.toJson(RegistrationData.invalidRegistration))

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must return Conflict when trying to insert a duplicate" in {

      val mockService = mock[RegistrationServiceEtmpImpl]
      when(mockService.createRegistration(any())(any(), any())) thenReturn Future.successful(AlreadyExists)

      val app =
        applicationBuilder
          .overrides(bind[RegistrationService].toInstance(mockService))
          .build()

      running(app) {

        val request =
          FakeRequest(POST, routes.RegistrationController.create().url)
            .withJsonBody(Json.toJson(RegistrationData.registration))

        val result = route(app, request).value

        status(result) mustEqual CONFLICT
      }
    }
  }

  "get" - {

    "must return OK and a registration when one is found" in {

      val mockService = mock[RegistrationServiceEtmpImpl]
      when(mockService.get(any())(any(), any())) thenReturn Future.successful(Some(RegistrationData.registration))

      val app =
        applicationBuilder
          .overrides(bind[RegistrationService].toInstance(mockService))
          .build()

      running(app) {
        val request = FakeRequest(GET, routes.RegistrationController.get().url)
        val result = route(app, request).value

        status(result) mustEqual OK
        contentAsJson(result) mustEqual Json.toJson(RegistrationData.registration)
      }
    }

    "must return NOT_FOUND when a registration is not found" - {

      val mockService = mock[RegistrationServiceEtmpImpl]
      when(mockService.get(any())(any(), any())) thenReturn Future.successful(None)

      val app =
        applicationBuilder
          .overrides(bind[RegistrationService].toInstance(mockService))
          .build()

      running(app) {
        val request = FakeRequest(GET, routes.RegistrationController.get().url)
        val result = route(app, request).value

        status(result) mustEqual NOT_FOUND
      }
    }
  }

  "get(vrn)" - {

    "must return OK and a registration when one is found" in {
      val mockService = mock[RegistrationService]
      when(mockService.get(any())(any(), any())) thenReturn Future.successful(Some(RegistrationData.registration))

      val app =
        applicationBuilder
          .overrides(bind[RegistrationService].toInstance(mockService))
          .build()

      running(app) {
        val request = FakeRequest(GET, routes.RegistrationController.getByVrn(vrn.vrn).url)
        val result = route(app, request).value

        status(result) mustEqual OK
        contentAsJson(result) mustEqual Json.toJson(RegistrationData.registration)
      }
    }

    "must return NOT_FOUND when a registration is not found" - {

      val mockService = mock[RegistrationService]
      when(mockService.get(any())(any(), any())) thenReturn Future.successful(None)

      val app =
        applicationBuilder
          .overrides(bind[RegistrationService].toInstance(mockService))
          .build()

      running(app) {
        val request = FakeRequest(GET, routes.RegistrationController.getByVrn(vrn.vrn).url)
        val result = route(app, request).value

        status(result) mustEqual NOT_FOUND
      }
    }
  }

  "amend" - {

    "must return 201 when given a valid payload and the registration is created successfully" in {

      val mockService = mock[RegistrationServiceEtmpImpl]
      when(mockService.amend(any())(any(), any())) thenReturn Future.successful(AmendSucceeded)
      when(mockService.get(any())(any(), any())) thenReturn Future.successful(Some(RegistrationData.registration))

      val app =
        applicationBuilder
          .overrides(bind[RegistrationService].toInstance(mockService))
          .build()

      running(app) {

        val request =
          FakeRequest(POST, routes.RegistrationController.amend().url)
            .withJsonBody(Json.toJson(RegistrationData.amendRegistrationPayload))

        val result = route(app, request).value

        status(result) mustEqual OK
      }
    }

    "must return 400 when the JSON request payload is not a registration" in {

      val app = applicationBuilder.build()

      running(app) {

        val request =
          FakeRequest(POST, routes.RegistrationController.amend().url)
            .withJsonBody(Json.toJson(RegistrationData.invalidRegistration))

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}
