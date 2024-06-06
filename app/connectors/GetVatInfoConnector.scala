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

import config.GetVatInfoConfig
import connectors.VatCustomerInfoHttpParser._
import logging.Logging
import metrics.{MetricsEnum, ServiceMetrics}
import models.GatewayTimeout
import play.api.http.HeaderNames
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier, HttpErrorFunctions, StringContextOps}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class GetVatInfoConnector @Inject()(getVatInfoConfig: GetVatInfoConfig, httpClientV2: HttpClientV2, metrics: ServiceMetrics)
                                   (implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

  private val XCorrelationId = "X-Correlation-Id"

  private def headers(correlationId: String) = Seq(
    HeaderNames.AUTHORIZATION -> s"Bearer ${getVatInfoConfig.authorizationToken}",
    "Environment" -> getVatInfoConfig.environment,
    XCorrelationId -> correlationId
  )

  def getVatCustomerDetails(vrn: Vrn)(implicit headerCarrier: HeaderCarrier): Future[VatCustomerInfoResponse] = {
    val url = url"${getVatInfoConfig.baseUrl}vat/customer/vrn/${vrn.value}/information"
    val timerContext = metrics.startTimer(MetricsEnum.GetVatCustomerDetails)
    val correlationId = UUID.randomUUID.toString

    httpClientV2.get(url).setHeader(headers(correlationId): _*).execute[VatCustomerInfoResponse].map { result =>
      timerContext.stop()
      result
    }.recover {
      case e: GatewayTimeoutException =>
        timerContext.stop()
        logger.error(s"Request timeout from Get vat info: $e", e)
        Left(GatewayTimeout)
    }
  }
}