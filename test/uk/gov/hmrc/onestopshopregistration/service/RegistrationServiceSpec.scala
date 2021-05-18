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

package uk.gov.hmrc.onestopshopregistration.service

import akka.http.scaladsl.util.FastFuture.successful
import org.mockito.Mockito._
import org.mockito.stubbing.ScalaFirstStubbing.toScalaFirstStubbing
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.onestopshopregistration.models.{BusinessAddress, BusinessContactDetails, Registration}
import uk.gov.hmrc.onestopshopregistration.repositories.RegistrationRepository

import java.time.LocalDate

class RegistrationServiceSpec extends AnyFreeSpec
  with Matchers with MockitoSugar {

  private val registration1          = mock[Registration]
  private val registration1saved     = mock[Registration]
  private val registrationRepository = mock[RegistrationRepository]

  private val service =
    new RegistrationService(
      registrationRepository
    )

  private val registration =
    Registration(
      "foo",
      true,
      Some(List("single", "double")),
      true,
      "GB123456789",
      LocalDate.now(),
      "AA1 1AA",
      true,
      Some(Map("France" -> "FR123456789", "Spain" -> "ES123456789")),
      new BusinessAddress(
        "123 Street",
        Some("Street"),
        "City",
        Some("county"),
        "AA12 1AB"
      ),
      new BusinessContactDetails(
        "Joe Bloggs",
        "01112223344",
        "email@email.com",
        "web.com"
      )
    )

  private final val emulatedFailure = new RuntimeException("Emulated failure.")

  "insert()" - {

    "return the case after it is inserted in the database collection" in {
      when(registrationRepository.insert(registration1)).thenReturn(successful(registration1saved))

      await(registrationRepository.insert(registration1)) shouldBe registration1saved
    }

//    "propagate any error" in {
//      when(registrationRepository.insert(registration1)).thenThrow(emulatedFailure)
//
//      val caught = intercept[RuntimeException] {
//        await(registrationRepository.insert(registration1))
//      }
//      caught shouldBe emulatedFailure
    }

}
