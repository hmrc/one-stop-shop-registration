/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.actions

import base.BaseSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.mvc.Results.Unauthorized
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import services.RegistrationService
import testutils.RegistrationData
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RegistrationRequiredActionSpec extends BaseSpec with MockitoSugar with EitherValues {

  class Harness(registrationService: RegistrationService) extends RegistrationRequiredAction(registrationService) {

    def callRefine[A](request: AuthorisedMandatoryVrnRequest[A]): Future[Either[Result, AuthorisedMandatoryRegistrationRequest[A]]] = refine(request)
  }

  "Registration Required Action" - {

    "return AuthorisedMandatoryRegistrationRequest when a registration is found" in {

      val mockRegistrationService = mock[RegistrationService]
      when(mockRegistrationService.get(eqTo(vrn))(any[HeaderCarrier], any())) thenReturn Future.successful(Some(RegistrationData.registration))

      val action = new Harness(mockRegistrationService)
      val request = FakeRequest(GET, "/test/url?k=session-id")
      val result = action.callRefine(AuthorisedMandatoryVrnRequest(request,
        userId,
        vrn)).futureValue

      val expectResult = AuthorisedMandatoryRegistrationRequest(request, userId, vrn, RegistrationData.registration)

      result mustBe Right(expectResult)

    }

    "must return Left Unauthorised when no registration is found" in {

      val mockRegistrationService = mock[RegistrationService]
      when(mockRegistrationService.get(eqTo(vrn))(any[HeaderCarrier], any())) thenReturn Future.successful(None)

      val action = new Harness(mockRegistrationService)
      val request = FakeRequest(GET, "/test/url?k=session-id")
      val result = action.callRefine(AuthorisedMandatoryVrnRequest(request,
        userId,
        vrn)).futureValue

      result mustBe Left(Unauthorized)

    }
  }

}