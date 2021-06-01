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

package uk.gov.hmrc.onestopshopregistration.connectors

import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.onestopshopregistration.logging.Logging
import uk.gov.hmrc.onestopshopregistration.models.des._

object VatCustomerInfoHttpParser extends Logging {

  type VatCustomerInfoResponse = Either[DesErrorResponse, VatCustomerInfo]

  implicit object VatCustomerInfoReads extends HttpReads[VatCustomerInfoResponse] {
    override def read(method: String, url: String, response: HttpResponse): VatCustomerInfoResponse =
      response.status match {
        case OK =>
          response.json.validate[VatCustomerInfo](VatCustomerInfo.desReads) match {
            case JsSuccess(model, _) => Right(model)
            case JsError(errors) =>
              logger.warn("Failed trying to parse JSON", errors)
              Left(InvalidJson)
          }
        case NOT_FOUND =>
          Left(NotFound)
        case INTERNAL_SERVER_ERROR =>
          Left(ServerError)
        case BAD_REQUEST =>
          Left(InvalidVrn)
        case SERVICE_UNAVAILABLE =>
          Left(ServiceUnavailable)
        case status =>
          logger.warn(s"Unexpected response from DES, received status $status")
          Left(UnexpectedResponseStatus(status, s"Unexpected response from DES, received status $status"))
      }
  }
}
