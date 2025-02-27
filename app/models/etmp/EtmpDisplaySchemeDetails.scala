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

package models.etmp

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, OWrites, Reads, __}

import java.time.format.DateTimeFormatter

case class EtmpDisplaySchemeDetails(
                              commencementDate: String,
                              requestDate: Option[String] = None,
                              registrationDate: Option[String] = None,
                              firstSaleDate: Option[String],
                              euRegistrationDetails: Seq[EtmpEuRegistrationDetails],
                              previousEURegistrationDetails: Seq[EtmpPreviousEURegistrationDetails],
                              onlineMarketPlace: Boolean,
                              websites: Seq[Website],
                              contactName: String,
                              businessTelephoneNumber: String,
                              businessEmailId: String,
                              nonCompliantReturns: Option[String],
                              nonCompliantPayments: Option[String],
                              exclusions: Seq[EtmpExclusion],
                              unusableStatus: Option[Boolean]
                            )

object EtmpDisplaySchemeDetails {

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  private def fromDisplayRegistrationPayload(
                                              commencementDate: String,
                                              requestDate: Option[String],
                                              registrationDate: Option[String],
                                              firstSaleDate: Option[String],
                                              euRegistrationDetails: Option[Seq[EtmpEuRegistrationDetails]],
                                              previousEURegistrationDetails: Option[Seq[EtmpPreviousEURegistrationDetails]],
                                              onlineMarketPlace: Boolean,
                                              websites: Option[Seq[Website]],
                                              contactNameOrBusinessAddress: String,
                                              businessTelephoneNumber: String,
                                              businessEmailAddress: String,
                                              nonCompliantReturns: Option[String],
                                              nonCompliantPayments: Option[String],
                                              exclusions: Option[Seq[EtmpExclusion]],
                                              unusableStatus: Option[Boolean]
                                            ): EtmpDisplaySchemeDetails =
    EtmpDisplaySchemeDetails(
      commencementDate = commencementDate,
      requestDate = requestDate,
      registrationDate = registrationDate,
      firstSaleDate = firstSaleDate,
      euRegistrationDetails = euRegistrationDetails.fold(Seq.empty[EtmpEuRegistrationDetails])(a => a),
      previousEURegistrationDetails = previousEURegistrationDetails.fold(Seq.empty[EtmpPreviousEURegistrationDetails])(a => a),
      onlineMarketPlace = onlineMarketPlace,
      websites = websites.fold(Seq.empty[Website])(a => a),
      contactName = contactNameOrBusinessAddress,
      businessTelephoneNumber = businessTelephoneNumber,
      businessEmailId = businessEmailAddress,
      nonCompliantReturns = nonCompliantReturns,
      nonCompliantPayments = nonCompliantPayments,
      exclusions = exclusions.fold(Seq.empty[EtmpExclusion])(a => a),
      unusableStatus = unusableStatus
    )

  implicit val reads: Reads[EtmpDisplaySchemeDetails] =
    (
      (__ \ "commencementDate").read[String] and
        (__ \ "requestDate").readNullable[String] and
        (__ \ "registrationDate").readNullable[String] and
        (__ \ "firstSaleDate").readNullable[String] and
        (__ \ "euRegistrationDetails").readNullable[Seq[EtmpEuRegistrationDetails]] and
        (__ \ "previousEURegistrationDetails").readNullable[Seq[EtmpPreviousEURegistrationDetails]] and
        (__ \ "onlineMarketPlace").read[Boolean] and
        (__ \ "websites").readNullable[Seq[Website]] and
        (__ \ "contactDetails" \ "contactNameOrBusinessAddress").read[String] and
        (__ \ "contactDetails" \ "businessTelephoneNumber").read[String] and
        (__ \ "contactDetails" \ "businessEmailAddress").read[String] and
        (__ \ "nonCompliantReturns").readNullable[String] and
        (__ \ "nonCompliantPayments").readNullable[String] and
        (__ \ "exclusions").readNullable[Seq[EtmpExclusion]] and
        (__ \ "unusableStatus").readNullable[Boolean]
      )(EtmpDisplaySchemeDetails.fromDisplayRegistrationPayload _)

  implicit val writes: OWrites[EtmpDisplaySchemeDetails] =
    Json.writes[EtmpDisplaySchemeDetails]
}