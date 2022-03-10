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

import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import logging.Logging
import models.{ErrorResponse, InvalidJson, InvalidVrn, NotFound, ServerError, ServiceUnavailable, UnexpectedResponseStatus}
import models.des._

object VatCustomerInfoHttpParser extends BaseHttpParser {

  override val serviceName: String = "DES"

  type VatCustomerInfoResponse = Either[ErrorResponse, VatCustomerInfo]

  implicit object VatCustomerInfoReads extends HttpReads[VatCustomerInfoResponse] {
    override def read(method: String, url: String, response: HttpResponse): VatCustomerInfoResponse =
      parseResponse[VatCustomerInfo](response)
  }
}
