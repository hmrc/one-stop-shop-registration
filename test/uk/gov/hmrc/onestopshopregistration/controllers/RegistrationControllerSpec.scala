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

package uk.gov.hmrc.onestopshopregistration.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import play.api.http.Status.CREATED
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.onestopshopregistration.base.BaseSpec
import uk.gov.hmrc.onestopshopregistration.service.RegistrationService
import utils.RegistrationData

import scala.concurrent.Future


class RegistrationControllerSpec extends BaseSpec {

  "create" - {

    "must return 201 when given a valid payload and when the registration is created successfully" in {

      val registration = RegistrationData.createNewRegistration()

      val mockService = mock[RegistrationService]
      when(mockService.insert(any())) thenReturn Future.successful(true)

      val app =
        new GuiceApplicationBuilder()
          .overrides(bind[RegistrationService].toInstance(mockService))
          .build()

      running(app) {

        val request =
          FakeRequest(POST, routes.RegistrationController.create().url)
            .withJsonBody(Json.toJson(registration))

        val result = route(app, request).value

        status(result) mustEqual CREATED
      }
    }

    "must return 400 when the JSON request payload is not a registration" in {

      val registration = RegistrationData.createInvalidRegistration()

      val mockService = mock[RegistrationService]
      when(mockService.insert(any())) thenReturn Future.successful(false)

      val app =
        new GuiceApplicationBuilder()
          .overrides(bind[RegistrationService].toInstance(mockService))
          .build()

      running(app) {

        val request =
          FakeRequest(POST, routes.RegistrationController.create().url)
            .withJsonBody(Json.toJson(registration))

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}