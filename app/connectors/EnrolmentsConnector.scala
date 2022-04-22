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
import connectors.EnrolmentsHttpParser.{EnrolmentResultsResponse, EnrolmentsResponseReads}
import logging.Logging
import play.api.http.HeaderNames
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class EnrolmentsConnector @Inject()(enrolments: EnrolmentsConfig, httpClient: HttpClient)
                                   (implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

  val headers = Seq(
    HeaderNames.AUTHORIZATION -> s"Bearer ${enrolments.authorizationToken}"
  )

  def assignEnrolment(userId: String, vrn: Vrn)(implicit headerCarrier: HeaderCarrier): Future[EnrolmentResultsResponse] = {
    val enrolmentKey = s"${enrolments.ossEnrolmentKey}~VRN~$vrn"
    val url = s"${enrolments.baseUrl}users/$userId/enrolments/$enrolmentKey"
    httpClient.GET[EnrolmentResultsResponse](url = url, headers = headers)
  }
}