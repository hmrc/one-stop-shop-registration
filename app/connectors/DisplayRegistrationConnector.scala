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

import config.DisplayRegistrationConfig
import connectors.DisplayRegistrationHttpParser._
import logging.Logging
import models.GatewayTimeout
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DisplayRegistrationConnector @Inject()(displayRegistrationConfig: DisplayRegistrationConfig, httpClient: HttpClient)(implicit ec: ExecutionContext) extends Logging {

  private val baseUrl = displayRegistrationConfig.displayRegistrationUrl

  private def headers: Seq[(String, String)] = displayRegistrationConfig.eisHeaders

  private val headersWithCorrelationId = headers

  def displayRegistration(vrn: Vrn)(implicit headerCarrier: HeaderCarrier): Future[DisplayRegistrationResponse] = {
    val url = s"${baseUrl}RESTAdapter/OSS/Subscription/${vrn.value}"
    httpClient.GET[DisplayRegistrationResponse](url = url, headers = headersWithCorrelationId).recover {
      case e: GatewayTimeoutException =>
        logger.error(s"Request timeout from EIS: $e", e)
        Left(GatewayTimeout)
    }
  }
}
