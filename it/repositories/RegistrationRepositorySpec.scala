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
import uk.gov.hmrc.mongo.test.{CleanMongoCollectionSupport, DefaultPlayMongoRepositorySupport}
import uk.gov.hmrc.onestopshopregistration.models.InsertResult.{AlreadyExists, InsertSucceeded}
import uk.gov.hmrc.onestopshopregistration.models._
import uk.gov.hmrc.onestopshopregistration.repositories.RegistrationRepository
import utils.RegistrationData

import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationRepositorySpec extends AnyFreeSpec
  with Matchers
  with DefaultPlayMongoRepositorySupport[Registration]
  with CleanMongoCollectionSupport
  with ScalaFutures
  with IntegrationPatience
  with OptionValues
  with MockitoSugar {

  override protected val repository = new RegistrationRepository(
      mongoComponent = mongoComponent
  )

  ".insert" - {

    "must insert a registration" in {


      val insertResult  = repository.insert(RegistrationData.registration).futureValue
      val updatedRecord = findAll().futureValue.headOption.value

      insertResult mustEqual InsertSucceeded
      updatedRecord mustEqual RegistrationData.registration
    }

    "must not allow duplicate registrations to be inserted" in {

      repository.insert(RegistrationData.registration).futureValue
      val secondResult = repository.insert(RegistrationData.registration).futureValue

      secondResult mustEqual AlreadyExists
    }
  }
}
