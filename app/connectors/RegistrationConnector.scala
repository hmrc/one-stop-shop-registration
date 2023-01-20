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

import config.IfConfig
import connectors.RegistrationHttpParser._
import logging.Logging
import models.UnexpectedResponseStatus
import models.etmp.EtmpRegistrationRequest
import models.requests.RegistrationRequest
import play.api.http.HeaderNames.AUTHORIZATION
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject()(
                                        httpClient: HttpClient,
                                        ifConfig: IfConfig
                                      )(implicit ec: ExecutionContext) extends Logging {

  private implicit val emptyHc: HeaderCarrier = HeaderCarrier()
  private def headers(correlationId: String): Seq[(String, String)] = ifConfig.ifHeaders(correlationId)

  def get(vrn: Vrn): Future[GetRegistrationResponse] = {

    val correlationId = UUID.randomUUID().toString
    val headersWithCorrelationId = headers(correlationId)

    httpClient.GET[GetRegistrationResponse](
      s"${ifConfig.baseUrl}getRegistration/${vrn.value}",
      headers = headersWithCorrelationId
    ).recover {
      case e: HttpException =>
        logger.error(s"Unexpected response from etmp registration, received status ${e.responseCode}", e)
        Left(UnexpectedResponseStatus(e.responseCode, s"Unexpected response from ${serviceName}, received status ${e.responseCode}"))
    }
  }

  def create(registration: RegistrationRequest): Future[CreateRegistrationResponse] = {

    val correlationId = UUID.randomUUID().toString
    val headersWithCorrelationId = headers(correlationId)

    val headersWithoutAuth = headersWithCorrelationId.filterNot{
      case (key, _) => key.matches(AUTHORIZATION)
    }

    logger.info(s"Sending request to etmp with headers $headersWithoutAuth")

    httpClient.POST[RegistrationRequest, CreateRegistrationResponse](
      s"${ifConfig.baseUrl}createRegistration",
      registration,
      headers = headersWithCorrelationId
    ).recover {
      case e: HttpException =>
        logger.error(s"Unexpected response from etmp registration, received status ${e.responseCode}", e)
        Left(UnexpectedResponseStatus(e.responseCode, s"Unexpected response from ${serviceName}, received status ${e.responseCode}"))
    }
  }

  def createWithEnrolment(registration: EtmpRegistrationRequest): Future[CreateRegistrationWithEnrolmentResponse] = {

    val correlationId = UUID.randomUUID().toString
    val headersWithCorrelationId = headers(correlationId)

    val headersWithoutAuth = headersWithCorrelationId.filterNot{
      case (key, _) => key.matches(AUTHORIZATION)
    }

    logger.info(s"Sending request to etmp with headers $headersWithoutAuth")

    httpClient.POST[EtmpRegistrationRequest, CreateRegistrationWithEnrolmentResponse](
      s"${ifConfig.baseUrl}createRegistration",
      registration,
      headers = headersWithCorrelationId
    ).recover {
      case e: HttpException =>
        logger.error(s"Unexpected response from etmp registration ${e.getMessage}", e)
        Left(UnexpectedResponseStatus(e.responseCode, s"Unexpected response from ${serviceName}, received status ${e.responseCode}"))
    }
  }

  def validateRegistration(vrn: Vrn): Future[ValidateRegistrationResponse] = {

    val correlationId = UUID.randomUUID().toString
    val headersWithCorrelationId = headers(correlationId)

    val url = s"${ifConfig.baseUrl}validateRegistration/${vrn.value}"
    httpClient.GET[ValidateRegistrationResponse](
      url = url,
      headers = headersWithCorrelationId
    ).recover {
      case e: HttpException =>
        logger.error(s"Unexpected response from core validate registration, received status ${e.responseCode}", e)
        Left(UnexpectedResponseStatus(e.responseCode, s"Unexpected response from ${serviceName}, received status ${e.responseCode}"))
    }
  }
}
