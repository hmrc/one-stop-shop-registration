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

package metrics

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer.Context

import javax.inject.Inject
import metrics.MetricsEnum.MetricsEnum
import uk.gov.hmrc.play.bootstrap.metrics.Metrics

class DefaultServiceMetrics @Inject()(val metrics: Metrics) extends ServiceMetrics
trait ServiceMetrics {
  val metrics: Metrics

  def startTimer(api: MetricsEnum): Context = timers(api).time()


  val registry: MetricRegistry = metrics.defaultRegistry
  val timers = Map(
    MetricsEnum.GetRegistration -> registry.timer("get-registration-response-timer"),
    MetricsEnum.CreateEtmpRegistration -> registry.timer("create-etmp-registration-response-timer"),
    MetricsEnum.ValidateCoreRegistration -> registry.timer("validate-core-registration-response-timer"),
    MetricsEnum.ConfirmEnrolment -> registry.timer("confirm-enrolment-response-timer"),
    MetricsEnum.GetVatCustomerDetails -> registry.timer("get-vat-customer-details-response-timer"),
    MetricsEnum.AmendRegistration -> registry.timer("get-amend-registration-response-timer")
  )

}