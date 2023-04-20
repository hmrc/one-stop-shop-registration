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

import config.AppConfig
import crypto.{RegistrationEncrypter, SecureGCMCipher}
import models._
import models.repository.AmendResult.AmendSucceeded
import models.repository.InsertResult.{AlreadyExists, InsertSucceeded}
import org.mockito.MockitoSugar
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import testutils.RegistrationData
import testutils.RegistrationData.registration
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.mongo.test.{CleanMongoCollectionSupport, PlayMongoRepositorySupport}

import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationRepositorySpec extends AnyFreeSpec
  with Matchers
  with PlayMongoRepositorySupport[EncryptedRegistration]
  with CleanMongoCollectionSupport
  with ScalaFutures
  with IntegrationPatience
  with OptionValues
  with MockitoSugar {

  private val cipher    = new SecureGCMCipher
  private val encrypter = new RegistrationEncrypter(cipher)
  private val appConfig = mock[AppConfig]
  private val secretKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="

  when(appConfig.encryptionKey) thenReturn secretKey

  override protected val repository = new RegistrationRepository(
      mongoComponent = mongoComponent,
      encrypter = encrypter,
      appConfig = appConfig
  )

  ".insert" - {

    "must insert a registration" in {

      val insertResult  = repository.insert(registration).futureValue
      val databaseRecord = findAll().futureValue.headOption.value
      val decryptedRecord = encrypter.decryptRegistration(databaseRecord, registration.vrn, secretKey)

      insertResult mustEqual InsertSucceeded
      decryptedRecord mustEqual registration
    }

    "must not allow duplicate registrations to be inserted" in {

      repository.insert(registration).futureValue
      val secondResult = repository.insert(registration).futureValue

      secondResult mustEqual AlreadyExists
    }
  }

  ".get" - {

    "must find a record when one exists" in {

      val registration = encrypter.encryptRegistration(RegistrationData.registration, RegistrationData.registration.vrn, secretKey)
      insert(registration).futureValue

      val result = repository.get(RegistrationData.registration.vrn).futureValue

      result.value mustEqual RegistrationData.registration
    }

    "must return None when no registration exists" in {

      val result = repository.get(registration.vrn).futureValue

      result must not be defined
    }
  }

  ".insertMany" - {

    "must insert registrations" in {
      repository.collection.drop().toFutureOption().futureValue
      val registration2 = registration.copy(vrn = Vrn("123456789"))
      val registrations = List(registration, registration2)

      val insertResult  = repository.insertMany(registrations).futureValue
      val databaseRecord1 = findAll().futureValue.headOption.value
      val databaseRecord2 = findAll().futureValue.lastOption.value
      val decryptedRecord1 = encrypter.decryptRegistration(databaseRecord1, registration.vrn, secretKey)
      val decryptedRecord2 = encrypter.decryptRegistration(databaseRecord2, registration2.vrn, secretKey)

      insertResult mustEqual InsertSucceeded
      decryptedRecord1 mustEqual registration
      decryptedRecord2 mustEqual registration2
    }

    "must not allow duplicate registrations to be inserted" in {
      val registration2 = registration.copy(vrn = Vrn("123456789"))
      val registrations = List(registration, registration2)
      repository.insertMany(registrations).futureValue
      val secondResult = repository.insertMany(registrations).futureValue

      secondResult mustEqual AlreadyExists
    }
  }

  ".update" - {

    "must update a registration" in {

      repository.insert(registration).futureValue
      val updatedRegistration = registration.copy(tradingNames = List("single", "double", "triple"))
      val updateResult = repository.set(updatedRegistration).futureValue

      val databaseRecord = findAll().futureValue.headOption.value
      val decryptedRecord = encrypter.decryptRegistration(databaseRecord, registration.vrn, secretKey)

      updateResult mustEqual AmendSucceeded
      decryptedRecord mustEqual updatedRegistration
    }

    "must not allow duplicate registrations to be inserted" in {

      repository.insert(registration).futureValue
      val secondResult = repository.insert(registration).futureValue

      secondResult mustEqual AlreadyExists
    }
  }
}
