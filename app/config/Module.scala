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

package config

import com.google.inject.AbstractModule
import controllers.actions.{AuthAction, AuthActionImpl, AuthenticatedControllerComponents, DefaultAuthenticatedControllerComponents}
import metrics.{DefaultServiceMetrics, ServiceMetrics}
import play.api.{Configuration, Environment}
import services.{HistoricalRegistrationEnrolmentService, HistoricalRegistrationEnrolmentServiceImpl, RegistrationService, RegistrationServiceEtmpImpl}

import java.time.{Clock, ZoneOffset}

class Module(environment: Environment, config: Configuration) extends AbstractModule {

  override def configure(): Unit = {

    bind(classOf[AuthAction]).to(classOf[AuthActionImpl]).asEagerSingleton()
    bind(classOf[ServiceMetrics]).to(classOf[DefaultServiceMetrics])
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))

    bind(classOf[AuthenticatedControllerComponents]).to(classOf[DefaultAuthenticatedControllerComponents]).asEagerSingleton()

    bind(classOf[RegistrationService]).to(classOf[RegistrationServiceEtmpImpl]).asEagerSingleton()
    bind(classOf[HistoricalRegistrationEnrolmentService]).to(classOf[HistoricalRegistrationEnrolmentServiceImpl]).asEagerSingleton()
  }
}
