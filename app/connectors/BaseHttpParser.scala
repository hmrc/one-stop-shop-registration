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

import logging.Logging
import models.{Conflict, ErrorResponse, InvalidJson, InvalidVrn, NotFound, Registration, ServerError, ServiceUnavailable, UnexpectedResponseStatus}
import play.api.http.Status.{BAD_REQUEST, CONFLICT, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.{JsError, JsSuccess, Reads}
import uk.gov.hmrc.http.HttpResponse

trait BaseHttpParser extends Logging {

  val serviceName: String

  def parseResponse[T](response: HttpResponse)(implicit rds: Reads[T]): Either[ErrorResponse, T] = {
    response.status match {
      case OK => response.json.validate[T] match {
          case JsSuccess(registration, _) => Right(registration)
          case JsError(errors) =>
            logger.warn("Failed trying to parse JSON", errors)
            Left(InvalidJson)
        }
      case NOT_FOUND =>
        logger.warn(s"Received NotFound from ${serviceName}")
        Left(NotFound)
      case INTERNAL_SERVER_ERROR =>
        logger.warn(s"Received InternalServerError from ${serviceName}")
        Left(ServerError)
      case BAD_REQUEST =>
        logger.error(s"Received BadRequest from ${serviceName}")
        Left(InvalidVrn)
      case SERVICE_UNAVAILABLE =>
        logger.warn(s"Received Service Unavailable from ${serviceName}")
        Left(ServiceUnavailable)
      case CONFLICT =>
        logger.warn(s"Received Conflict from ${serviceName}")
        Left(Conflict)
      case status =>
        logger.warn(s"Unexpected response from ${serviceName}, received status $status")
        Left(UnexpectedResponseStatus(status, s"Unexpected response from ${serviceName}, received status $status"))
    }
  }
}
