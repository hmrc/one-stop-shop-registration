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

import config.CoreValidationConfig
import connectors.ValidateCoreRegistrationHttpParser.{ValidateCoreRegistrationReads, ValidateCoreRegistrationResponse}
import logging.Logging
import models.core.{CoreRegistrationRequest, EisErrorResponse}
import models.EisError
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions, HttpException}

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ValidateCoreRegistrationConnector @Inject()(
                                                   coreValidationConfig: CoreValidationConfig,
                                                   httpClient: HttpClient
                                                 )(implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

  private implicit val emptyHc: HeaderCarrier = HeaderCarrier()

  private val baseUrl = coreValidationConfig.coreValidationUrl

  private def headers(correlationId: String): Seq[(String, String)] = coreValidationConfig.eisCoreHeaders(correlationId)

  def validateCoreRegistration(
                                coreRegistrationRequest: CoreRegistrationRequest
                              ): Future[ValidateCoreRegistrationResponse] = {
    val correlationId: String = UUID.randomUUID().toString
    val headersWithCorrelationId = headers(correlationId)

    val headersWithoutAuth = headersWithCorrelationId.filterNot {
      case (key, _) => key.matches(AUTHORIZATION)
    }

    logger.info(s"Sending request to EIS with headers $headersWithoutAuth and request ${Json.toJson(coreRegistrationRequest)}")

    val url = s"$baseUrl"
    httpClient.POST[CoreRegistrationRequest, ValidateCoreRegistrationResponse](
      url,
      coreRegistrationRequest,
      headers = headersWithCorrelationId
    ).recover {
      case e: HttpException =>
        val selfGeneratedRandomUUID = UUID.randomUUID()
        logger.error(
          s"Unexpected error response from EIS $url, received status ${e.responseCode}," +
            s"body of response was: ${e.message} with self-generated CorrelationId $selfGeneratedRandomUUID " +
            s"and original correlation ID we tried to pass $correlationId"
        )
        Left(EisError(
          EisErrorResponse(Instant.now(), s"UNEXPECTED_${e.responseCode.toString}", e.message)
        )
        )
    }
  }

}

