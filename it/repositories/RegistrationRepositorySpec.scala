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

package repositories

import org.mockito.MockitoSugar
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import uk.gov.hmrc.onestopshopregistration.models.{BusinessAddress, BusinessContactDetails, Registration, StartDate}
import uk.gov.hmrc.onestopshopregistration.repositories.RegistrationRepository

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global


class RegistrationRepositorySpec extends AnyFreeSpec
  with Matchers
  with DefaultPlayMongoRepositorySupport[Registration]
  with ScalaFutures
  with IntegrationPatience
  with OptionValues
  with MockitoSugar {

  override protected val repository = new RegistrationRepository(
      mongoComponent = mongoComponent
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
      StartDate(LocalDate.now()),
      new BusinessAddress(
        "123 Street",
        Some("Street"),
        "City",
        Some("county"),
        "AA12 1AB"
    ),
      List("website1", "website2"),
    new BusinessContactDetails(
      "Joe Bloggs",
      "01112223344",
      "email@email.com"
    )
  )

  ".insert" - {

    "must insert a registration" in {

      val insertResult  = repository.insert(registration).futureValue
      val updatedRecord = findAll().futureValue.headOption.value

      insertResult mustEqual true
      updatedRecord mustEqual registration
    }

  }
}
