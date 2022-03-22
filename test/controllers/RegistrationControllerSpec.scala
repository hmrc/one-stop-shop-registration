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
import models.InsertResult.{AlreadyExists, InsertSucceeded}
import models.TaxEnrolmentErrorResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import play.api.http.Status.CREATED
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{RegistrationService, RegistrationServiceRepositoryImpl}
import testutils.RegistrationData

import scala.concurrent.Future


class RegistrationControllerSpec extends BaseSpec {

  "create" - {

    "must return 201 when given a valid payload, the registration is created successfully and enrolment is added" in {

      val mockService = mock[RegistrationServiceRepositoryImpl]
      when(mockService.createRegistration(any())) thenReturn Future.successful(InsertSucceeded)
      when(mockService.addEnrolment(any(), any())(any())) thenReturn Future.successful(Right())

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

    "must return InternalServerError when given a valid payload, the registration is created successfully but adding an enrolment fails" in {

      val mockService = mock[RegistrationServiceRepositoryImpl]
      when(mockService.createRegistration(any())) thenReturn Future.successful(InsertSucceeded)
      when(mockService.addEnrolment(any(), any())(any())) thenReturn Future.successful(Left(TaxEnrolmentErrorResponse("123", "Error")))

      val app =
        applicationBuilder
          .overrides(bind[RegistrationService].toInstance(mockService))
          .build()

      running(app) {

        val request =
          FakeRequest(POST, routes.RegistrationController.create().url)
            .withJsonBody(Json.toJson(RegistrationData.registration))

        val result = route(app, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
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

      val mockService = mock[RegistrationServiceRepositoryImpl]
      when(mockService.createRegistration(any())) thenReturn Future.successful(AlreadyExists)

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

      val mockService = mock[RegistrationServiceRepositoryImpl]
      when(mockService.get(any())) thenReturn Future.successful(Some(RegistrationData.registration))

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

      val mockService = mock[RegistrationServiceRepositoryImpl]
      when(mockService.get(any())) thenReturn Future.successful(None)

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
      when(mockService.get(any())) thenReturn Future.successful(Some(RegistrationData.registration))

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
      when(mockService.get(any())) thenReturn Future.successful(None)

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
}