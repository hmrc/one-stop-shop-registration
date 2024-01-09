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

package services

import connectors.ValidateCoreRegistrationConnector
import logging.Logging
import models.core.{CoreRegistrationRequest, Match, SourceType}
import models.{CountryWithValidationDetails, PreviousScheme}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CoreValidationService @Inject()(
                                       connector: ValidateCoreRegistrationConnector
                                     )(implicit ec: ExecutionContext) extends Logging {

  def searchScheme(searchNumber: String, previousScheme: PreviousScheme, intermediaryNumber: Option[String], countryCode: String)
                  (implicit hc: HeaderCarrier): Future[Option[Match]] = {

    if (previousScheme == PreviousScheme.OSSNU) {
      Future.successful(None)
    } else {

      val sourceType = SourceType.TraderId

      val convertedSearchNumber = if (Seq(PreviousScheme.OSSU, PreviousScheme.OSSNU).contains(previousScheme)) {
        convertTaxIdentifierForTransfer(searchNumber, countryCode)
      } else {
        searchNumber
      }

      val coreRegistrationRequest = CoreRegistrationRequest(
        sourceType.toString,
        Some(convertScheme(previousScheme)),
        convertedSearchNumber,
        intermediaryNumber,
        countryCode
      )

      connector.validateCoreRegistration(coreRegistrationRequest).map {
        case Right(coreRegistrationResponse) =>
          coreRegistrationResponse.matches.headOption
        case _ => throw new Exception("Error while validating core registration")
      }
    }
  }

  private def convertScheme(previousScheme: PreviousScheme): String = {
    previousScheme match {
      case PreviousScheme.OSSU => "OSS"
      case PreviousScheme.OSSNU => "OSS"
      case PreviousScheme.IOSSWOI => "IOSS"
      case PreviousScheme.IOSSWI => "IOSS"
    }
  }


  private def convertTaxIdentifierForTransfer(identifier: String, countryCode: String): String = {

    CountryWithValidationDetails.euCountriesWithVRNValidationRules.find(_.country.code == countryCode) match {
      case Some(countryValidationDetails) =>
        if (identifier.matches(countryValidationDetails.vrnRegex)) {
          identifier.substring(2)
        } else {
          identifier
        }

      case _ =>
        logger.error("Error occurred while getting country code regex, unable to convert identifier")
        throw new IllegalStateException("Error occurred while getting country code regex, unable to convert identifier")
    }
  }

}
