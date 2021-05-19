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

import org.mockito.MockitoSugar.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.CREATED
import play.api.libs.json.Json.toJson
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.onestopshopregistration.models.Registration
import uk.gov.hmrc.onestopshopregistration.service.RegistrationService
import uk.gov.hmrc.play.test.UnitSpec
import utils.RegistrationData

import scala.concurrent.Future.successful

class RegistrationControllerSpec extends AnyFreeSpec
  with Matchers with MockitoSugar with UnitSpec {

  private val fakeRequest = FakeRequest("GET", "/")

  private val registrationService = mock[RegistrationService]

  private val registrationController = new RegistrationController(Helpers.stubControllerComponents(), registrationService)

  private val newRegistration: Registration = RegistrationData.createNewRegistration()

  "create()" - {

    "return 201 when the registration has been created successfully" in {
      when(registrationService.insert(newRegistration)).thenReturn(successful(true))

      val result = await(registrationController.create()(fakeRequest.withBody(toJson(newRegistration))))

      status(result) shouldEqual CREATED
      jsonBodyOf(result) shouldEqual successful(newRegistration)
    }
  }


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