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

package models.etmp

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, OWrites, Reads, __}

import java.time.format.DateTimeFormatter

//TODO Extra fields that will need to be pulled in from display - Suggest a trait
case class EtmpSchemeDetails(commencementDate: String,
                             firstSaleDate: Option[String],
                             euRegistrationDetails: Seq[EtmpEuRegistrationDetails],
                             previousEURegistrationDetails: Seq[EtmpPreviousEURegistrationDetails],
                             onlineMarketPlace: Boolean,
                             websites: Seq[Website],
                             contactName: String,
                             businessTelephoneNumber: String,
                             businessEmailId: String,
                             nonCompliantReturns: Option[Int],
                             nonCompliantPayments: Option[Int])

object EtmpSchemeDetails {

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  private def fromDisplayRegistrationPayload(
                                              commencementDate: String,
                                              firstSaleDate: Option[String],
                                              euRegistrationDetails: Option[Seq[EtmpEuRegistrationDetails]],
                                              previousEURegistrationDetails: Option[Seq[EtmpPreviousEURegistrationDetails]],
                                              onlineMarketPlace: Boolean,
                                              websites: Option[Seq[Website]],
                                              contactNameOrBusinessAddress: String,
                                              businessTelephoneNumber: String,
                                              businessEmailAddress: String,
                                              nonCompliantReturns: Option[Int],
                                              nonCompliantPayments: Option[Int]
                                            ): EtmpSchemeDetails =
    EtmpSchemeDetails(
      commencementDate = commencementDate,
      firstSaleDate = firstSaleDate,
      euRegistrationDetails = euRegistrationDetails.fold(Seq.empty[EtmpEuRegistrationDetails])(a => a),
      previousEURegistrationDetails = previousEURegistrationDetails.fold(Seq.empty[EtmpPreviousEURegistrationDetails])(a => a),
      onlineMarketPlace = onlineMarketPlace,
      websites = websites.fold(Seq.empty[Website])(a => a),
      contactName = contactNameOrBusinessAddress,
      businessTelephoneNumber = businessTelephoneNumber,
      businessEmailId = businessEmailAddress,
      nonCompliantReturns = nonCompliantReturns,
      nonCompliantPayments = nonCompliantPayments
    )

  implicit val reads: Reads[EtmpSchemeDetails] =
    (
      (__ \ "commencementDate").read[String] and
        (__ \ "firstSaleDate").readNullable[String] and
        (__ \ "euRegistrationDetails").readNullable[Seq[EtmpEuRegistrationDetails]] and
        (__ \ "previousEURegistrationDetails").readNullable[Seq[EtmpPreviousEURegistrationDetails]] and
        (__ \ "onlineMarketPlace").read[Boolean] and
        (__ \ "websites").readNullable[Seq[Website]] and
        (__ \ "contactDetails" \ "contactNameOrBusinessAddress").read[String] and
        (__ \ "contactDetails" \ "businessTelephoneNumber").read[String] and
        (__ \ "contactDetails" \ "businessEmailAddress").read[String] and
        (__ \ "nonCompliantReturns").readNullable[Int] and
        (__ \ "nonCompliantPayments").readNullable[Int]
      ) (EtmpSchemeDetails.fromDisplayRegistrationPayload _)

  implicit val writes: OWrites[EtmpSchemeDetails] =
    Json.writes[EtmpSchemeDetails]

}