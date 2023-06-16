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

package models.audit

import controllers.actions.AuthorisedMandatoryVrnRequest
import models.enrolments.EtmpEnrolmentResponse
import models.etmp.{AmendRegistrationResponse, DisplayRegistration, EtmpRegistrationRequest}
import models.Registration
import models.exclusions.ExcludedTrader
import play.api.libs.json.{JsObject, Json, JsValue}

case class EtmpDisplayRegistrationAuditModel(
                                              etmpRegistrationAuditType: EtmpRegistrationAuditType,
                                              userId: String,
                                              userAgent: String,
                                              vrn: String,
                                              etmpDisplayRegistration: DisplayRegistration,
                                              convertedRegistration: Registration
                                     ) extends JsonAuditModel {

  override val auditType: String = etmpRegistrationAuditType.auditType

  override val transactionName: String = etmpRegistrationAuditType.transactionName

  override val detail: JsValue = Json.obj(
    "userId" -> userId,
    "browserUserAgent" -> userAgent,
    "requestersVrn" -> vrn,
    "etmpDisplayRegistration" -> Json.toJson(etmpDisplayRegistration),
    "convertedRegistration" -> Json.toJson(convertedRegistration)
  )
}

object EtmpDisplayRegistrationAuditModel {
  def build(
             etmpRegistrationAuditType: EtmpRegistrationAuditType,
             etmpDisplayRegistration: DisplayRegistration,
             convertedRegistration: Registration
           )(implicit request: AuthorisedMandatoryVrnRequest[_]): EtmpDisplayRegistrationAuditModel =
    EtmpDisplayRegistrationAuditModel(
      etmpRegistrationAuditType = etmpRegistrationAuditType,
      userId = request.userId,
      userAgent = request.headers.get("user-agent").getOrElse(""),
      vrn = request.vrn.vrn,
      etmpDisplayRegistration = etmpDisplayRegistration,
      convertedRegistration = convertedRegistration
    )
}