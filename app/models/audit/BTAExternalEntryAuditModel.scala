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

import controllers.actions.AuthorisedRequest
import play.api.libs.json.{Json, JsValue}

case class BtaExternalEntryAuditModel(
                                       userId: String,
                                       userAgent: String,
                                       vrn: String
                                     ) extends JsonAuditModel {

  override val auditType: String = "BTAExternalEntry"
  override val transactionName: String = "bta-external-entry"

  override val detail: JsValue = Json.obj(
    "userId" -> userId,
    "browserUserAgent" -> userAgent,
    "vrn" -> vrn
  )
}

object BtaExternalEntryAuditModel {

  def build()(implicit request: AuthorisedRequest[_]): BtaExternalEntryAuditModel =
    BtaExternalEntryAuditModel(
      userId = request.userId,
      userAgent = request.headers.get("user-agent").getOrElse(""),
      vrn = request.vrn.vrn
    )
}