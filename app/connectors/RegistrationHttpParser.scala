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

package connectors

import models._
import models.core.EisDisplayErrorResponse
import models.enrolments.{EtmpEnrolmentErrorResponse, EtmpEnrolmentResponse}
import models.etmp.{AmendRegistrationResponse, DisplayRegistration}
import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object RegistrationHttpParser extends BaseHttpParser {

  override val serviceName: String = "etmp registration"

  type CreateRegistrationResponse = Either[ErrorResponse, Unit]

  type CreateEtmpRegistrationResponse = Either[ErrorResponse, EtmpEnrolmentResponse]

  type DisplayRegistrationResponse = Either[ErrorResponse, DisplayRegistration]

  type CreateAmendRegistrationResponse = Either[ErrorResponse, AmendRegistrationResponse]

  implicit object CreateRegistrationResponseReads extends HttpReads[CreateRegistrationResponse]  {
    override def read(method: String, url: String, response: HttpResponse): CreateRegistrationResponse =
      response.status match {
        case ACCEPTED =>
          Right(())
        case NOT_FOUND =>
          logger.warn(s"Received NotFound from ${serviceName} ${response.body}")
          Left(NotFound)
        case CONFLICT =>
          logger.warn(s"Received Conflict from ${serviceName} ${response.body}")
          Left(Conflict)
        case INTERNAL_SERVER_ERROR =>
          logger.warn(s"Received InternalServerError from ${serviceName} ${response.body}")
          Left(ServerError)
        case BAD_REQUEST =>
          logger.error(s"Received BadRequest from ${serviceName} ${response.body}")
          Left(InvalidVrn)
        case SERVICE_UNAVAILABLE =>
          logger.error(s"Received Service Unavailable from ${serviceName} ${response.body}")
          Left(ServiceUnavailable)
        case status =>
          logger.error(s"Unexpected response from core registration, received status $status ${response.body}")
          Left(UnexpectedResponseStatus(status, s"Unexpected response from ${serviceName}, received status $status"))
      }
  }

  implicit object CreateRegistrationWithEnrolment extends HttpReads[CreateEtmpRegistrationResponse] {
    override def read(method: String, url: String, response: HttpResponse): CreateEtmpRegistrationResponse =
      response.status match {
        case CREATED => response.json.validate[EtmpEnrolmentResponse] match {
          case JsSuccess(enrolmentResponse, _) => Right(enrolmentResponse)
          case JsError(errors) =>
            logger.error(s"Failed trying to parse JSON, but was successfully created ${response.body} ${errors}", errors)
            Left(InvalidJson)
        }
        case status =>
          if(response.body.nonEmpty) {
            response.json.validate[EtmpEnrolmentErrorResponse] match {
              case JsSuccess(enrolmentResponse, _) => Left(EtmpEnrolmentError(enrolmentResponse.errorDetail.errorCode, enrolmentResponse.errorDetail.errorMessage))
              case JsError(errors) =>
                logger.error(s"Failed trying to parse JSON with status ${response.status} and body ${response.body}", errors)
                logger.warn(s"Unexpected response from core registration, received status $status")
                Left(UnexpectedResponseStatus(status, s"Unexpected response from ${serviceName}, received status $status"))
            }
          } else {
            logger.error(s"Failed trying to parse empty JSON with status ${response.status} and body ${response.body}")
            logger.warn(s"Unexpected response from core registration, received status $status")
            Left(UnexpectedResponseStatus(status, s"Unexpected response from ${serviceName}, received status $status"))
          }
      }
  }

  implicit object DisplayRegistrationReads extends HttpReads[DisplayRegistrationResponse] {
    override def read(method: String, url: String, response: HttpResponse): DisplayRegistrationResponse =
      response.status match {
        case OK => response.json.validate[DisplayRegistration] match {
          case JsSuccess(displayRegistrationResponse, _) => Right(displayRegistrationResponse)
          case JsError(errors) =>
            logger.error(s"Failed trying to parse display registration response JSON with status ${response.status} with errors: $errors")
            Left(InvalidJson)
        }
        case UNPROCESSABLE_ENTITY =>
          logger.debug("Unprocessable entity, attempting to parse error")

          response.json.validate[EisDisplayErrorResponse] match {
            case JsSuccess(eisDisplayErrorResponse, _) =>
              Left(EisDisplayRegistrationError(eisDisplayErrorResponse))
            case JsError(errors) =>
              logger.error(s"Failed trying to parse display error registration response JSON with status ${response.status} and body ${response.body}", errors)
              Left(ServerError)

          }

        case NOT_FOUND =>
          logger.warn(s"Url not reachable")
          Left(NotFound)
        case status =>
          logger.error(s"Unknown error happened on display registration $status with body ${response.body}")
          Left(ServerError)
      }
  }

  implicit object CreateAmendRegistrationResponseReads extends HttpReads[CreateAmendRegistrationResponse] {
    override def read(method: String, url: String, response: HttpResponse): CreateAmendRegistrationResponse =
      response.status match {
        case OK => response.json.validate[AmendRegistrationResponse] match {
          case JsSuccess(amendRegistrationResponse, _) => Right(amendRegistrationResponse)
          case JsError(errors) =>
            logger.error(s"Failed trying to parse JSON with status ${response.status} and body ${response.body}", errors)
            Left(InvalidJson)
        }
        case NOT_FOUND =>
          logger.warn(s"url not reachable")
          Left(NotFound)
        case status =>
          logger.error(s"Unknown error happened on amend registration $status with body ${response.body}")
          Left(ServerError)
      }
  }

}
