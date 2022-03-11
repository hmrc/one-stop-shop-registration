/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.RegistrationConnector
import logging.Logging
import models.InsertResult.{AlreadyExists, InsertSucceeded}
import models.requests.RegistrationRequest
import models.{Conflict, EtmpException, InsertResult, Registration}
import uk.gov.hmrc.domain.Vrn

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationServiceEtmpImpl @Inject()(
                                             registrationConnector: RegistrationConnector
                                           )(implicit ec: ExecutionContext) extends RegistrationService with Logging {

  def createRegistration(request: RegistrationRequest): Future[InsertResult] =
    registrationConnector.create(request).map {
      case Right(_) => InsertSucceeded
      case Left(Conflict) => AlreadyExists
      case Left(error) => throw EtmpException(s"There was an error getting Registration from ETMP: ${error.body}")
    }

  def get(vrn: Vrn): Future[Option[Registration]] = {
    registrationConnector.get(vrn).map {
      case Right(registration) => Some(registration)
      case Left(error) =>
        logger.error(s"There was an error getting Registration from ETMP: ${error.body}")
        None
    }
  }
}