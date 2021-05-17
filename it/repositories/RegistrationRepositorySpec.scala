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

import akka.actor.TypedActor.dispatcher
import org.mockito.MockitoSugar
import org.mongodb.scala.model.Filters
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import uk.gov.hmrc.onestopshopregistration.models.Registration
import uk.gov.hmrc.onestopshopregistration.repositories.RegistrationRepository


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

  private val registration = Registration("registeredCompanyName", Json.obj("foo" -> "bar"))

  ".set" - {
    "must set the last updated time on the supplied user answers to `now`, and save them" in {

      val expectedResult = registration copy()

      val setResult     = repository.set(registration).futureValue
      val updatedRecord = find(Filters.equal("_registeredCompanyName", registration.registeredCompanyName)).futureValue.headOption.value

      setResult mustEqual true
      updatedRecord mustEqual expectedResult
    }
  }
}