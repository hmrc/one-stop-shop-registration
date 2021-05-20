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
  }


//  private val fakeRequest = FakeRequest("GET", "/")
//
//  private val registrationService = mock[RegistrationService]
//
//  private val registrationController = new RegistrationController(Helpers.stubControllerComponents(), registrationService)
//
//  private val newRegistration: Registration = RegistrationData.createNewRegistration()
//
//  "create()" - {
//
//    "return 201 when the registration has been created successfully" in {
//      when(registrationService.insert(newRegistration)).thenReturn(successful(true))
//
//      val result = await(registrationController.create()(fakeRequest.withBody(toJson(newRegistration))))
//
//      status(result) shouldEqual CREATED
//      jsonBodyOf(result) shouldEqual successful(newRegistration)
//    }
//  }


}

//"create()" should {
//
//  "return 201 when the case has been created successfully" in {
//  when(caseService.nextCaseReference(ApplicationType.BTI)).thenReturn(successful("1"))
//  when(caseService.insert(any[Case])).thenReturn(successful(c1))
//  when(caseService.addInitialSampleStatusIfExists(any[Case])).thenReturn(Future.successful((): Unit))
//
//  val result = await(controller.create()(fakeRequest.withBody(toJson(newCase))))
//
//  status(result) shouldEqual CREATED
//  jsonBodyOf(result) shouldEqual toJson(c1)
//}
//
//  "return 400 when the JSON request payload is not a Case" in {
//  val body   = """{"a":"b"}"""
//  val result = await(controller.create()(fakeRequest.withBody(toJson(body))))
//
//  status(result) shouldEqual BAD_REQUEST
//}
//
//  "return 500 when an error occurred" in {
//  val error = new DatabaseException {
//  override def originalDocument: Option[BSONDocument] = None
//  override def code: Option[Int]                      = Some(11000)
//  override def message: String                        = "duplicate value for db index"
//}
//
//  when(caseService.nextCaseReference(ApplicationType.BTI)).thenReturn(successful("1"))
//  when(caseService.insert(any[Case])).thenReturn(failed(error))
//
//  val result = await(controller.create()(fakeRequest.withBody(toJson(newCase))))
//
//  status(result) shouldEqual INTERNAL_SERVER_ERROR
//  jsonBodyOf(result).toString() shouldEqual """{"code":"UNKNOWN_ERROR","message":"An unexpected error occurred"}"""
//}
//
//}