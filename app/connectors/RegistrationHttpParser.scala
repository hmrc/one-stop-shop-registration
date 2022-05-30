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

package connectors

import models.{Conflict, ErrorResponse, InvalidVrn, NotFound, Registration, ServerError, ServiceUnavailable, UnexpectedResponseStatus, ValidateRegistration}
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object RegistrationHttpParser extends BaseHttpParser {

  override val serviceName: String = "core registration"

  type CreateRegistrationResponse = Either[ErrorResponse, Unit]

  type GetRegistrationResponse = Either[ErrorResponse, Registration]

  type ValidateRegistrationResponse = Either[ErrorResponse, ValidateRegistration]

  implicit object CreateRegistrationResponseReads extends HttpReads[CreateRegistrationResponse]  {
    override def read(method: String, url: String, response: HttpResponse): CreateRegistrationResponse =
      response.status match {
        case ACCEPTED =>
          Right(())
        case NOT_FOUND =>
          logger.warn(s"Received NotFound from ${serviceName}")
          Left(NotFound)
        case CONFLICT =>
          logger.warn(s"Received Conflict from ${serviceName}")
          Left(Conflict)
        case INTERNAL_SERVER_ERROR =>
          logger.warn(s"Received InternalServerError from ${serviceName}")
          Left(ServerError)
        case BAD_REQUEST =>
          logger.error(s"Received BadRequest from ${serviceName}")
          Left(InvalidVrn)
        case SERVICE_UNAVAILABLE =>
          logger.warn(s"Received Service Unavailable from ${serviceName}")
          Left(ServiceUnavailable)
        case status =>
          logger.warn(s"Unexpected response from core registration, received status $status")
          Left(UnexpectedResponseStatus(status, s"Unexpected response from ${serviceName}, received status $status"))
      }
  }

  implicit object GetRegistrationResponseReads extends HttpReads[GetRegistrationResponse]  {
    override def read(method: String, url: String, response: HttpResponse): GetRegistrationResponse =
      parseResponse[Registration](response)
  }

  implicit object ValidateRegistrationReads extends HttpReads[ValidateRegistrationResponse] {
    override def read(method: String, url: String, response: HttpResponse): ValidateRegistrationResponse =
      parseResponse[ValidateRegistration](response)
  }
}
