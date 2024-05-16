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

package models.audit

import models.{Enumerable, WithName}

sealed trait EtmpRegistrationAuditType {
  val auditType: String
  val transactionName: String
}

object EtmpRegistrationAuditType extends Enumerable.Implicits {
  case object CreateRegistration extends WithName("CreateRegistration") with EtmpRegistrationAuditType {
    override val auditType: String = "EtmpRegistration"
    override val transactionName: String = "etmp-registration"
  }
  case object AmendRegistration extends WithName("AmendRegistration") with EtmpRegistrationAuditType {
    override val auditType: String = "EtmpRegistrationAmended"
    override val transactionName: String = "etmp-registration-amended"
  }
  case object DisplayRegistration extends WithName("EtmpDisplayRegistration") with EtmpRegistrationAuditType {
    override val auditType: String = "EtmpRegistrationDisplay"
    override val transactionName: String = "etmp-registration-display"
  }
}