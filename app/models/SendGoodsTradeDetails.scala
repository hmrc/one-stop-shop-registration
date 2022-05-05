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

package models

import crypto.EncryptedValue
import play.api.libs.json.{Json, OFormat}

final case class SendGoodsTradeDetails(tradingName: String, address: InternationalAddress)

object SendGoodsTradeDetails {

  implicit val format: OFormat[SendGoodsTradeDetails] = Json.format[SendGoodsTradeDetails]
}

final case class EncryptedSendGoodsTradeDetails(
                                              tradingName: EncryptedValue,
                                              address: EncryptedInternationalAddress
                                            )

object EncryptedSendGoodsTradeDetails {

  implicit val format: OFormat[EncryptedSendGoodsTradeDetails] = Json.format[EncryptedSendGoodsTradeDetails]
}