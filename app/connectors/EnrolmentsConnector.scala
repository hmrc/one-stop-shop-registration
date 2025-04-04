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

import config.{EnrolmentStoreProxyConfig, EnrolmentsConfig}
import logging.Logging
import metrics.{MetricsEnum, ServiceMetrics}
import models.binders.Format.enrolmentDateFormatter
import models.enrolments.{ES8Request, SubscriberRequest}
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, HttpResponse, StringContextOps}
import play.api.libs.ws.writeableOf_JsValue

import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnrolmentsConnector @Inject()(
                                     enrolments: EnrolmentsConfig,
                                     enrolmentStoreProxyConfig: EnrolmentStoreProxyConfig,
                                     httpClientV2: HttpClientV2,
                                     metrics: ServiceMetrics)
                                   (implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

  def confirmEnrolment(subscriptionId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val timerContext = metrics.startTimer(MetricsEnum.ConfirmEnrolment)
    val etmpId = UUID.randomUUID().toString

    httpClientV2
      .put(url"${enrolments.baseUrl}subscriptions/$subscriptionId/subscriber")
      .withBody(Json.toJson(SubscriberRequest(enrolments.ossEnrolmentKey,
        s"${enrolments.callbackBaseUrl}${controllers.routes.EnrolmentsSubscriptionController.authoriseEnrolment(subscriptionId).url}",
        etmpId
      ))).execute[HttpResponse].map { result =>
      timerContext.stop()
      result
    }
  }

  def es8(groupId: String, vrn: Vrn, userId: String, registrationDate: LocalDate): Future[HttpResponse] = {

    implicit val emptyHc: HeaderCarrier = HeaderCarrier()

    val friendlyName = "OSS Subscription"
    val `type` = "principal"
    val action = "enrolAndActivate"
    val enrolmentKey = s"HMRC-OSS-ORG~VRN~$vrn"

    val url = url"${enrolmentStoreProxyConfig.baseUrl}enrolment-store/groups/$groupId/enrolments/$enrolmentKey"

    val ossRegistrationDate = "OSSRegistrationDate"

    val requestPayload = ES8Request(userId,
      friendlyName,
      `type`,
      action,
      Seq(Map("key" -> ossRegistrationDate, "value" -> registrationDate.format(enrolmentDateFormatter)))
    )

    val jsonPayload = Json.toJson(requestPayload)

    logger.info(s"Sending payload $requestPayload and json: $jsonPayload to url $url")

    httpClientV2.post(url).withBody(Json.toJson(requestPayload)).execute[HttpResponse]
  }
}