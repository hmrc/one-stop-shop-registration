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

import config.{AmendRegistrationConfig, DisplayRegistrationConfig, IfConfig}
import connectors.RegistrationHttpParser._
import logging.Logging
import metrics.{MetricsEnum, ServiceMetrics}
import models.UnexpectedResponseStatus
import models.etmp.EtmpRegistrationRequest
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, StringContextOps}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject()(
                                       httpClientV2: HttpClientV2,
                                       ifConfig: IfConfig,
                                       amendRegistrationConfig: AmendRegistrationConfig,
                                       displayRegistrationConfig: DisplayRegistrationConfig,
                                       metrics: ServiceMetrics
                                     )(implicit ec: ExecutionContext) extends Logging {

  private implicit val emptyHc: HeaderCarrier = HeaderCarrier()

  private def getHeaders(correlationId: String): Seq[(String, String)] = displayRegistrationConfig.eisEtmpGetHeaders(correlationId)

  private def createHeaders(correlationId: String): Seq[(String, String)] = ifConfig.eisEtmpCreateHeaders(correlationId)

  private def amendHeaders(correlationId: String): Seq[(String, String)] = amendRegistrationConfig.eisEtmpAmendHeaders(correlationId)

  def get(vrn: Vrn): Future[DisplayRegistrationResponse] = {

    val correlationId = UUID.randomUUID.toString
    val headersWithCorrelationId = getHeaders(correlationId)
    val timerContext = metrics.startTimer(MetricsEnum.GetRegistration)
    val url = url"${displayRegistrationConfig.baseUrl}vec/ossregistration/viewreg/v1/${vrn.value}"
    httpClientV2.get(url).setHeader(headersWithCorrelationId: _*).execute[DisplayRegistrationResponse].map { result =>
      timerContext.stop()
      result
    }.recover {
      case e: HttpException =>
        timerContext.stop()
        logger.error(s"Unexpected response from etmp registration ${e.getMessage}", e)
        Left(UnexpectedResponseStatus(e.responseCode, s"Unexpected response from ${serviceName}, received status ${e.responseCode}"))
    }
  }

  def create(registration: EtmpRegistrationRequest): Future[CreateEtmpRegistrationResponse] = {

    val correlationId = UUID.randomUUID.toString
    val headersWithCorrelationId = createHeaders(correlationId)
    val timerContext = metrics.startTimer(MetricsEnum.CreateEtmpRegistration)
    val headersWithoutAuth = headersWithCorrelationId.filterNot {
      case (key, _) => key.matches(AUTHORIZATION)
    }

    logger.info(s"Sending create request to etmp with headers $headersWithoutAuth")

    httpClientV2.post(url"${ifConfig.baseUrl}vec/ossregistration/regdatatransfer/v1")
      .setHeader(headersWithCorrelationId: _*)
      .withBody(Json.toJson(registration))
      .execute[CreateEtmpRegistrationResponse]
      .map { result =>
        timerContext.stop()
        result
      }.recover {
      case e: HttpException =>
        timerContext.stop()
        logger.error(s"Unexpected response from etmp registration ${e.getMessage}", e)
        Left(UnexpectedResponseStatus(e.responseCode, s"Unexpected response from ${serviceName}, received status ${e.responseCode}"))
    }
  }

  def amendRegistration(registration: EtmpRegistrationRequest): Future[CreateAmendRegistrationResponse] = {

    val correlationId: String = UUID.randomUUID.toString
    val headersWithCorrelationId = amendHeaders(correlationId)
    val timerContext = metrics.startTimer(MetricsEnum.AmendRegistration)
    val headersWithoutAuth = headersWithCorrelationId.filterNot {
      case (key, _) => key.matches(AUTHORIZATION)
    }

    logger.info(s"Sending amend request to etmp with headers $headersWithoutAuth")

    httpClientV2.put(url"${amendRegistrationConfig.baseUrl}vec/ossregistration/amendreg/v1")
      .setHeader(headersWithCorrelationId: _*)
      .withBody(Json.toJson(registration))
      .execute[CreateAmendRegistrationResponse]
      .map { result =>
        timerContext.stop()
        result
      }.recover {
      case e: HttpException =>
        timerContext.stop()
        logger.error(s"Unexpected response from etmp registration ${e.getMessage}", e)
        Left(UnexpectedResponseStatus(e.responseCode, s"Unexpected response from ${serviceName}, received status ${e.responseCode}"))
    }
  }

}
