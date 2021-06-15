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

package connectors

import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpClient, HttpErrorFunctions}
import config.DesConfig
import connectors.VatCustomerInfoHttpParser._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import logging.Logging

class DesConnector @Inject()(des: DesConfig, httpClient: HttpClient)
                            (implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

  implicit val hc: HeaderCarrier =
    HeaderCarrier(authorization = Some(Authorization(s"Bearer ${des.authorizationToken}")))
      .withExtraHeaders("Environment" -> des.environment)

  def getVatCustomerDetails(vrn: Vrn): Future[VatCustomerInfoResponse] = {
    httpClient.GET[VatCustomerInfoResponse](s"${des.baseUrl}vat/customer/vrn/${vrn.value}/information")
  }
}
