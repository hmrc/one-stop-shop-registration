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

package services

import base.BaseSpec
import connectors.RegistrationConnector
import models.InsertResult.{AlreadyExists, InsertSucceeded}
import models.{Conflict, EtmpException, NotFound, ServiceUnavailable}
import models.requests.RegistrationRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import testutils.RegistrationData.registration
import uk.gov.hmrc.domain.Vrn

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class RegistrationServiceEtmpImplSpec extends BaseSpec with BeforeAndAfterEach {

  private val registrationRequest = mock[RegistrationRequest]
  private val registrationConnector = mock[RegistrationConnector]

  private val service = new RegistrationServiceEtmpImpl(registrationConnector)

  override def beforeEach(): Unit = {
    reset(registrationConnector)
    super.beforeEach()
  }

  ".createRegistration" - {

    "must create a registration from the request, save it and return the result of the save operation" in {

      when(registrationConnector.create(any())) thenReturn Future.successful(Right())

      service.createRegistration(registrationRequest).futureValue mustEqual InsertSucceeded
    }

    "must return Already Exists when connector returns conflict" in {
      when(registrationConnector.create(any())) thenReturn Future.successful(Left(Conflict))

      service.createRegistration(registrationRequest).futureValue mustBe AlreadyExists
    }

    "must throw Exception when connector returns any other error" in {
      when(registrationConnector.create(any())) thenReturn Future.successful(Left(ServiceUnavailable))

      whenReady(service.createRegistration(registrationRequest).failed) {
        exp => exp mustBe EtmpException(s"There was an error getting Registration from ETMP: ${ServiceUnavailable.body}")
      }
    }
  }

  ".get" - {

    "must return a Some(registration) when the connector returns right" in {
      when(registrationConnector.get(any())) thenReturn Future.successful(Right(registration))
      service.get(Vrn("123456789")).futureValue mustBe Some(registration)
      verify(registrationConnector, times(1)).get(Vrn("123456789"))
    }

    "must return a None when the connector returns Left(error)" in {
      when(registrationConnector.get(any())) thenReturn Future.successful(Left(NotFound))
      service.get(Vrn("123456789")).futureValue mustBe None
      verify(registrationConnector, times(1)).get(Vrn("123456789"))
    }
  }
}