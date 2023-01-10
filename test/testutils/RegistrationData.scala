package testutils

import models.EuTaxIdentifierType.Vat
import models.VatDetailSource.UserEntered
import models._
import models.requests.RegistrationRequest
import uk.gov.hmrc.domain.Vrn

import java.time.{Clock, Instant, LocalDate, ZoneId}

object RegistrationData {

  val stubClock: Clock = Clock.fixed(LocalDate.now.atStartOfDay(ZoneId.systemDefault).toInstant, ZoneId.systemDefault)
  val iban: Iban = Iban("GB33BUKB20201555555555").right.get
  val bic: Bic = Bic("ABCDGB2A").get

  val registration: Registration =
    Registration(
      vrn = Vrn("123456789"),
      registeredCompanyName = "foo",
      tradingNames = List("single", "double"),
      vatDetails = VatDetails(
        registrationDate = LocalDate.now,
        address = DesAddress(
          "123 Street",
          Some("Street"),
          Some("City"),
          Some("county"),
          None,
          Some("AA12 1AB"),
          "GB",
        ),
        partOfVatGroup = true,
        source = UserEntered
      ),
      euRegistrations = Seq(
        RegistrationWithoutFixedEstablishment(
          Country("FR", "France"),
          EuTaxIdentifier(Vat, "FR123")
        ),
        RegistrationWithFixedEstablishment(
          Country("DE", "Germany"),
          EuTaxIdentifier(Vat, "DE123"),
          TradeDetails("Name", InternationalAddress("Line 1", None, "Town", None, None, Country("FR", "France")))
        )
      ),
      contactDetails = new ContactDetails(
        "Joe Bloggs",
        "01112223344",
        "email@email.com"
      ),
      websites = List("website1", "website2"),
      commencementDate = LocalDate.now,
      previousRegistrations = Seq(
        PreviousRegistration(
          country = Country("DE", "Germany"),
          previousSchemesDetails = Seq(
            PreviousSchemeDetails(
              previousScheme = PreviousScheme.OSSU,
              previousSchemeNumbers = PreviousSchemeNumbers(
                previousSchemeNumber = "DE123",
                previousIntermediaryNumber = None
              )
            )
          )
        )
      ),
      bankDetails = BankDetails("Account name", Some(bic), iban),
      isOnlineMarketplace = false,
      niPresence = Some(PrincipalPlaceOfBusinessInNi),
      dateOfFirstSale = Some(LocalDate.now),
      submissionReceived = Instant.now(stubClock),
      lastUpdated = Instant.now(stubClock),
      nonCompliantReturns = Some(1),
      nonCompliantPayments = Some(2)
    )

  val invalidRegistration = """{"invalidName":"invalid"}"""

  def toRegistrationRequest(registration: Registration) = {
    RegistrationRequest(
      registration.vrn,
      registration.registeredCompanyName,
      registration.tradingNames,
      registration.vatDetails,
      registration.euRegistrations,
      registration.contactDetails,
      registration.websites,
      registration.commencementDate,
      registration.previousRegistrations,
      registration.bankDetails,
      registration.isOnlineMarketplace,
      registration.niPresence,
      registration.dateOfFirstSale,
      registration.nonCompliantReturns,
      registration.nonCompliantPayments
    )
  }

}
