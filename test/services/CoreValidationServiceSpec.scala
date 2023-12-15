/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.ValidateCoreRegistrationConnector
import models.{PreviousScheme, UnexpectedResponseStatus}
import models.core.{CoreRegistrationValidationResult, Match, MatchType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CoreValidationServiceSpec extends BaseSpec with MockitoSugar with ScalaFutures with Matchers with BeforeAndAfterEach {

  private val genericMatch = Match(
    MatchType.FixedEstablishmentActiveNETP,
    "333333333",
    None,
    "EE",
    Some(2),
    None,
    None,
    None,
    None
  )

  private val coreValidationResponses: CoreRegistrationValidationResult =
    CoreRegistrationValidationResult(
      "333333333",
      None,
      "EE",
      traderFound = true,
      Seq(
        genericMatch
      ))

  private val connector = mock[ValidateCoreRegistrationConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "coreRegistrationValidationService.searchScheme" - {

    "call searchScheme with correct ioss number and must return match data" in {

      val iossNumber: String = "333333333"
      val countryCode: String = "DE"
      val previousScheme: PreviousScheme = PreviousScheme.OSSU

      when(connector.validateCoreRegistration(any())) thenReturn Future.successful(Right(coreValidationResponses))

      val coreRegistrationValidationService = new CoreValidationService(connector)

      val value = coreRegistrationValidationService.searchScheme(iossNumber, previousScheme, None, countryCode).futureValue.get

      value equals genericMatch
    }

    "call searchScheme with correct ioss number with intermediary and must return match data" in {

      val iossNumber: String = "IM333222111"
      val intermediaryNumber: String = "IN555444222"
      val countryCode: String = "DE"
      val previousScheme: PreviousScheme = PreviousScheme.OSSU

      when(connector.validateCoreRegistration(any())) thenReturn Future.successful(Right(coreValidationResponses))

      val coreRegistrationValidationService = new CoreValidationService(connector)

      val value = coreRegistrationValidationService.searchScheme(iossNumber, previousScheme, Some(intermediaryNumber), countryCode).futureValue.get

      value equals genericMatch
    }

    "must return None when no match found" in {

      val iossNumber: String = "333333333"
      val countryCode: String = "DE"
      val previousScheme: PreviousScheme = PreviousScheme.OSSU

      val expectedResponse = coreValidationResponses.copy(matches = Seq[Match]())
      when(connector.validateCoreRegistration(any())) thenReturn Future.successful(Right(expectedResponse))

      val coreRegistrationValidationService = new CoreValidationService(connector)

      val value = coreRegistrationValidationService.searchScheme(iossNumber, previousScheme, None, countryCode).futureValue

      value mustBe None
    }

    "must return exception when server responds with an error" in {

      val iossNumber: String = "333333333"
      val countryCode: String = "DE"
      val previousScheme: PreviousScheme = PreviousScheme.OSSU

      val errorCode = Gen.oneOf(BAD_REQUEST, NOT_FOUND, INTERNAL_SERVER_ERROR).sample.value

      when(connector.validateCoreRegistration(any())) thenReturn Future.successful(Left(UnexpectedResponseStatus(errorCode, "error")))

      val coreRegistrationValidationService = new CoreValidationService(connector)

      val response = intercept[Exception](coreRegistrationValidationService.searchScheme(iossNumber, previousScheme, None, countryCode).futureValue)

      response.getMessage must include("Error while validating core registration")
    }
  }

}
