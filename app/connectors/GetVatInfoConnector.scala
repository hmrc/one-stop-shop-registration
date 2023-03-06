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

package connectors

import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier, HttpClient, HttpErrorFunctions}
import config.GetVatInfoConfig
import connectors.VatCustomerInfoHttpParser._
import logging.Logging
import models.GatewayTimeout

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.http.HeaderNames


class GetVatInfoConnector @Inject()(getVatInfoConfig: GetVatInfoConfig, httpClient: HttpClient)
                                   (implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

  private val headers = Seq(
    HeaderNames.AUTHORIZATION -> s"Bearer ${getVatInfoConfig.authorizationToken}",
    "Environment" -> getVatInfoConfig.environment
  )

  def getVatCustomerDetails(vrn: Vrn)(implicit headerCarrier: HeaderCarrier): Future[VatCustomerInfoResponse] = {
    val url = s"${getVatInfoConfig.baseUrl}vat/customer/vrn/${vrn.value}/information"
    httpClient.GET[VatCustomerInfoResponse](url = url, headers = headers).recover{
      case e: GatewayTimeoutException =>
        logger.warn(s"Request timeout from Get vat info: $e")
        Left(GatewayTimeout)
    }
  }
}