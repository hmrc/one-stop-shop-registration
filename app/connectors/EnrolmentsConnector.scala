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

import config.EnrolmentsConfig
import logging.Logging
import models.enrolments.SubscriberRequest
import play.api.http.HeaderNames
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions, HttpResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class EnrolmentsConnector @Inject()(enrolments: EnrolmentsConfig, httpClient: HttpClient)
                                   (implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

  private implicit val emptyHc: HeaderCarrier = HeaderCarrier()
  val headers = Seq(
    HeaderNames.AUTHORIZATION -> s"Bearer ${enrolments.authorizationToken}"
  )

  def confirmEnrolment(subscriptionId: String): Future[HttpResponse] = {

    val etmpId = UUID.randomUUID().toString

    httpClient.PUT[SubscriberRequest, HttpResponse](
      s"${enrolments.baseUrl}subscriptions/$subscriptionId/subscriber",
      SubscriberRequest(enrolments.ossEnrolmentKey,
        controllers.routes.EnrolmentsSubscriptionController.authoriseEnrolment(subscriptionId).url,
        etmpId
      ),
      headers)
  }
}