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
import play.api.{Configuration, Environment}
import services.{HistoricalRegistrationEnrolmentService, HistoricalRegistrationEnrolmentServiceImpl, RegistrationService, RegistrationServiceEtmpImpl, RegistrationServiceRepositoryImpl}
import metrics.{DefaultServiceMetrics, ServiceMetrics}

import java.time.{Clock, ZoneOffset}

class Module(environment: Environment, config: Configuration) extends AbstractModule {

  override def configure(): Unit = {

    val sendRegToEtmp: Boolean = config.get[Boolean]("features.sendRegToEtmp")

    bind(classOf[AuthAction]).to(classOf[AuthActionImpl]).asEagerSingleton()
    bind(classOf[ServiceMetrics]).to(classOf[DefaultServiceMetrics])
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))

    bind(classOf[AuthenticatedControllerComponents]).to(classOf[DefaultAuthenticatedControllerComponents]).asEagerSingleton()

    if(sendRegToEtmp) {
      bind(classOf[RegistrationService]).to(classOf[RegistrationServiceEtmpImpl]).asEagerSingleton()
    } else {
      bind(classOf[RegistrationService]).to(classOf[RegistrationServiceRepositoryImpl]).asEagerSingleton()
    }
    bind(classOf[HistoricalRegistrationEnrolmentService]).to(classOf[HistoricalRegistrationEnrolmentServiceImpl]).asEagerSingleton()
  }
}
