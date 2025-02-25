/*
 * Copyright 2025 HM Revenue & Customs
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

import config.ChannelPreferenceConfig
import models.etmp.channelPreference.ChannelPreferenceRequest
import play.api.http.HeaderNames
import play.api.libs.json.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, HttpResponse, StringContextOps}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.writeableOf_JsValue

class ChannelPreferenceConnector @Inject()(
                                            httpClientV2: HttpClientV2,
                                            channelPreferenceConfig: ChannelPreferenceConfig
                                          )(implicit executionContext: ExecutionContext) extends HttpErrorFunctions {

  private val XCorrelationId = "X-Correlation-Id"

  private def headers(correlationId: String): Seq[(String, String)] = Seq(
    HeaderNames.AUTHORIZATION -> s"Bearer ${channelPreferenceConfig.authorizationToken}",
    "Environment" -> channelPreferenceConfig.environment,
    XCorrelationId -> correlationId
  )

  def updatePreferences(channelPreference: ChannelPreferenceRequest)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    val correlationId: String = UUID.randomUUID.toString
    httpClientV2.put(url"${channelPreferenceConfig.baseUrl}income-tax/customer/OSS/contact-preference")
      .withBody(Json.toJson(channelPreference))
      .setHeader(headers(correlationId): _*)
      .execute[HttpResponse]
  }
}
