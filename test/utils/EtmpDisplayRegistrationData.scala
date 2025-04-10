package utils

import base.BaseSpec
import models.etmp.*
import org.scalacheck.Gen
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{OWrites, __}

object EtmpDisplayRegistrationData extends BaseSpec {

  val arbitraryEtmpDisplayRegistration: EtmpDisplayRegistration =
    EtmpDisplayRegistration(
      tradingNames = Gen.listOfN(3, arbitraryEtmpTradingNames.arbitrary).sample.value,
      schemeDetails = arbitraryEtmpDisplauSchemeDetails.arbitrary.sample.value,
      bankDetails = arbitraryBankDetails.arbitrary.sample.value,
      adminUse = arbitraryAdminUse.arbitrary.sample.value
    )

  implicit val writesEtmpEuRegistrationDetails: OWrites[EtmpEuRegistrationDetails] = {
    (
      (__ \ "issuedBy").write[String] and
        (__ \ "vatNumber").writeNullable[String] and
        (__ \ "taxIdentificationNumber").writeNullable[String] and
        (__ \ "fixedEstablishment").writeNullable[Boolean] and
        (__ \ "fixedEstablishmentTradingName").writeNullable[String] and
        (__ \ "fixedEstablishmentAddressLine1").writeNullable[String] and
        (__ \ "fixedEstablishmentAddressLine2").writeNullable[String] and
        (__ \ "townOrCity").writeNullable[String] and
        (__ \ "regionOrState").writeNullable[String] and
        (__ \ "postcode").writeNullable[String]
      ) (etmpEuRegistrationDetails => Tuple.fromProductTyped(etmpEuRegistrationDetails))
  }

  implicit val writesEtmpSchemeDetails: OWrites[EtmpDisplaySchemeDetails] = {
    (
      (__ \ "commencementDate").write[String] and
        (__ \ "requestDate").writeNullable[String] and
        (__ \ "registrationDate").writeNullable[String] and
        (__ \ "firstSaleDate").writeNullable[String] and
        (__ \ "euRegistrationDetails").write[Seq[EtmpEuRegistrationDetails]] and
        (__ \ "previousEURegistrationDetails").write[Seq[EtmpPreviousEURegistrationDetails]] and
        (__ \ "onlineMarketPlace").write[Boolean] and
        (__ \ "websites").write[Seq[Website]] and
        (__ \ "contactDetails" \ "contactNameOrBusinessAddress").write[String] and
        (__ \ "contactDetails" \ "businessTelephoneNumber").write[String] and
        (__ \ "contactDetails" \ "businessEmailAddress").write[String] and
        (__ \ "nonCompliantReturns").writeNullable[String] and
        (__ \ "nonCompliantPayments").writeNullable[String] and
        (__ \ "exclusions").write[Seq[EtmpExclusion]] and
        (__ \ "unusableStatus").writeNullable[Boolean]
      ) (etmpDisplaySchemeDetails => Tuple.fromProductTyped(etmpDisplaySchemeDetails))
  }
}
