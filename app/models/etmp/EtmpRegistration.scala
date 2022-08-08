package models.etmp

import models.{BankDetails, ContactDetails, EuTaxRegistration, NiPresence, PreviousRegistration, VatDetails}
import uk.gov.hmrc.domain.Vrn

import java.time.{Instant, LocalDate}

case class EtmpRegistration(
                             vrn: Vrn,
                             tradingNames: Seq[EtmpTradingNames],
                             vatDetails: VatDetails,
                             schemaDetails: EtmpSchemeDetails,
                             euRegistrations: Seq[EuTaxRegistration],
                             contactDetails: ContactDetails,
                             websites: Seq[String],
                             commencementDate: LocalDate,
                             previousRegistrations: Seq[PreviousRegistration],
                             bankDetails: BankDetails,
                             isOnlineMarketplace: Boolean,
                             niPresence: Option[NiPresence],
                             dateOfFirstSale: Option[LocalDate],
                             submissionReceived: Instant,
                             lastUpdated: Instant
                           ) {
}
