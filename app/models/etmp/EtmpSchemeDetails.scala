package models.etmp

case class EtmpSchemeDetails(commencementDate: String,
                             firstSaleDate: Option[String],
                             euRegistrationDetails: Option[EtmpEuRegistrationDetails],
                             previousEURegistrationDetails: Option[EtmpPreviousEURegistrationDetails],
                             onlineMarketPlace: Boolean,
                             websites: Seq[Website],
                             contactName: String,
                             businessTelephoneNumber: String,
                             businessEmailId: String)
