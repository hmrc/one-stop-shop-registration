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

import config.IfConfig
import connectors.RegistrationHttpParser.RegistrationResponse
import connectors.RegistrationHttpParser._
import logging.Logging
import models.Registration
import models.requests.RegistrationRequest
import play.api.http.HeaderNames.AUTHORIZATION
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject()(
                                        httpClient: HttpClient,
                                        ifConfig: IfConfig
                                      )(implicit ec: ExecutionContext) extends Logging {

  private implicit val emptyHc: HeaderCarrier = HeaderCarrier()
  private def headers(correlationId: String): Seq[(String, String)] = ifConfig.ifHeaders(correlationId)

  def get(vrn: Vrn): Future[Registration] = {

    val correlationId = UUID.randomUUID().toString
    val headersWithCorrelationId = headers(correlationId)

    httpClient.GET[Registration](
      s"${ifConfig.baseUrl}getRegistration/${vrn.value}",
      headers = headersWithCorrelationId
    )
  }

  def create(registration: RegistrationRequest): Future[RegistrationResponse] = {

    val correlationId = UUID.randomUUID().toString
    val headersWithCorrelationId = headers(correlationId)

    val headersWithoutAuth = headersWithCorrelationId.filterNot{
      case (key, _) => key.matches(AUTHORIZATION)
    }

    logger.info(s"Sending request to core with headers $headersWithoutAuth")

    httpClient.POST[RegistrationRequest, RegistrationResponse](
      s"${ifConfig.baseUrl}createRegistration",
      registration,
      headers = headersWithCorrelationId
    )
  }
}
