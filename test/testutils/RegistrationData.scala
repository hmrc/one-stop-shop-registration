package testutils

import models.EuTaxIdentifierType.{Other, Vat}
import models.VatDetailSource.{Etmp, UserEntered}
import models._
import models.etmp.EtmpSchemeDetails.dateFormatter
import models.etmp._
import models.requests.RegistrationRequest
import repositories.RegistrationWrapper
import uk.gov.hmrc.domain.Vrn

import java.time._

object RegistrationData {

  val stubClock: Clock = Clock.fixed(LocalDate.now.atStartOfDay(ZoneId.systemDefault).toInstant, ZoneId.systemDefault)
  val iban: Iban = Iban("GB33BUKB20201555555555").toOption.get
  val bic: Bic = Bic("ABCDGB2A").get
  private val userId = "id-123"

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
        RegistrationWithoutTaxId(
          Country("FR", "France")
        ),
        RegistrationWithFixedEstablishment(
          Country("DE", "Germany"),
          EuTaxIdentifier(Vat, "DE123"),
          TradeDetails("Name", InternationalAddress("Line 1", None, "Town", None, None, Country("DE", "Germany")))
        ),
        RegistrationWithoutFixedEstablishmentWithTradeDetails(
          Country("BE", "Belgium"),
          EuTaxIdentifier(Other, "12345"),
          TradeDetails("Name", InternationalAddress("Line 1", Some("Line 2"), "Town", None, None, Country("BE", "Belgium")))
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
        PreviousRegistrationNew(
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
        ),
        PreviousRegistrationLegacy(
          country = Country("BE", "Belgium"),
          vatNumber = "BE123"
        ),
        PreviousRegistrationNew(
          country = Country("EE", "Estonia"),
          previousSchemesDetails = Seq(
            PreviousSchemeDetails(
              previousScheme = PreviousScheme.OSSNU,
              previousSchemeNumbers = PreviousSchemeNumbers(
                previousSchemeNumber = "EE123",
                previousIntermediaryNumber = None
              )
            ),
            PreviousSchemeDetails(
              previousScheme = PreviousScheme.IOSSWI,
              previousSchemeNumbers = PreviousSchemeNumbers(
                previousSchemeNumber = "EE234",
                previousIntermediaryNumber = Some("IN234")
              )
            ),
            PreviousSchemeDetails(
              previousScheme = PreviousScheme.IOSSWOI,
              previousSchemeNumbers = PreviousSchemeNumbers(
                previousSchemeNumber = "EE312",
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
      submissionReceived = Some(Instant.now(stubClock)),
      lastUpdated = Some(Instant.now(stubClock)),
      nonCompliantReturns = Some("1"),
      nonCompliantPayments = Some("2"),
      adminUse = AdminUse(Some(LocalDateTime.now(stubClock)))
    )

  val fromEtmpRegistration: Registration =
    Registration(
      vrn = Vrn("123456789"),
      registeredCompanyName = "Company name",
      tradingNames = List("single", "double"),
      vatDetails = VatDetails(
        registrationDate = LocalDate.now,
        address = DesAddress(
          "Line 1",
          None,
          None,
          None,
          None,
          Some("AA11 1AA"),
          "GB",
        ),
        partOfVatGroup = false,
        source = Etmp
      ),
      euRegistrations = Seq(
        EuVatRegistration(
          Country("ES", "Spain"),
          "ES123"
        ),
        RegistrationWithoutTaxId(
          Country("FR", "France")
        ),
        RegistrationWithFixedEstablishment(
          Country("DE", "Germany"),
          EuTaxIdentifier(Vat, "DE123"),
          TradeDetails("Name", InternationalAddress("Line 1", Some("Line 2"), "Town", None, None, Country("DE", "Germany")))
        ),
        RegistrationWithoutFixedEstablishmentWithTradeDetails(
          Country("BE", "Belgium"),
          EuTaxIdentifier(Other, "12345"),
          TradeDetails("Name", InternationalAddress("Line 1", Some("Line 2"), "Town", Some("Region"), Some("Postcode"), Country("BE", "Belgium")))
        )
      ),
      contactDetails = new ContactDetails(
        "Joe Bloggs",
        "01112223344",
        "email@email.com"
      ),
      websites = List("website1", "website2"),
      commencementDate = LocalDate.of(2023, 1, 1),
      previousRegistrations = Seq(
        PreviousRegistrationNew(
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
        ),
        PreviousRegistrationNew(
          country = Country("BE", "Belgium"),
          previousSchemesDetails = Seq(
            PreviousSchemeDetails(
              previousScheme = PreviousScheme.OSSNU,
              previousSchemeNumbers = PreviousSchemeNumbers(
                previousSchemeNumber = "BE123",
                previousIntermediaryNumber = None
              )
            )
          )
        ),
        PreviousRegistrationNew(
          country = Country("EE", "Estonia"),
          previousSchemesDetails = Seq(
            PreviousSchemeDetails(
              previousScheme = PreviousScheme.OSSNU,
              previousSchemeNumbers = PreviousSchemeNumbers(
                previousSchemeNumber = "EE123",
                previousIntermediaryNumber = None
              )
            ),
            PreviousSchemeDetails(
              previousScheme = PreviousScheme.IOSSWI,
              previousSchemeNumbers = PreviousSchemeNumbers(
                previousSchemeNumber = "EE234",
                previousIntermediaryNumber = Some("IN234")
              )
            ),
            PreviousSchemeDetails(
              previousScheme = PreviousScheme.IOSSWOI,
              previousSchemeNumbers = PreviousSchemeNumbers(
                previousSchemeNumber = "EE312",
                previousIntermediaryNumber = None
              )
            )
          )
        )
      ),
      bankDetails = BankDetails("Account name", Some(bic), iban),
      isOnlineMarketplace = false,
      niPresence = None,
      dateOfFirstSale = Some(LocalDate.of(2023, 1, 25)),
      submissionReceived = None,
      lastUpdated = None,
      nonCompliantReturns = Some("1"),
      nonCompliantPayments = Some("2"),
      adminUse = AdminUse(Some(LocalDateTime.now(stubClock)))
    )

  val wrappedCachedRegistration: RegistrationWrapper = RegistrationWrapper(userId, Some(fromEtmpRegistration), Instant.now(stubClock))

  val displayRegistration: EtmpDisplayRegistration =
    EtmpDisplayRegistration(
      tradingNames = Seq(EtmpTradingNames("single"), EtmpTradingNames("double")),
      schemeDetails = EtmpDisplaySchemeDetails(
        commencementDate = LocalDate.of(2023, 1, 1).format(dateFormatter),
        firstSaleDate = Some(LocalDate.of(2023, 1, 25).format(dateFormatter)),
        euRegistrationDetails = Seq(
          EtmpEuRegistrationDetails(
            countryOfRegistration = "ES",
            vatNumber = Some("ES123")
          ),
          EtmpEuRegistrationDetails(
            countryOfRegistration = "FR"
          ),
          EtmpEuRegistrationDetails(
            countryOfRegistration = "DE",
            vatNumber = Some("DE123"),
            fixedEstablishment = Some(true),
            tradingName = Some("Name"),
            fixedEstablishmentAddressLine1 = Some("Line 1"),
            fixedEstablishmentAddressLine2 = Some("Line 2"),
            townOrCity = Some("Town"),
          ),
          EtmpEuRegistrationDetails(
            countryOfRegistration = "BE",
            taxIdentificationNumber = Some("12345"),
            fixedEstablishment = Some(false),
            tradingName = Some("Name"),
            fixedEstablishmentAddressLine1 = Some("Line 1"),
            fixedEstablishmentAddressLine2 = Some("Line 2"),
            townOrCity = Some("Town"),
            regionOrState = Some("Region"),
            postcode = Some("Postcode")
          )
        ),
        previousEURegistrationDetails = Seq(
          EtmpPreviousEURegistrationDetails(
            issuedBy = "DE",
            registrationNumber = "DE123",
            schemeType = SchemeType.OSSUnion,
            intermediaryNumber = None
          ),
          EtmpPreviousEURegistrationDetails(
            issuedBy = "BE",
            registrationNumber = "BE123",
            schemeType = SchemeType.OSSNonUnion,
            intermediaryNumber = None
          ),
          EtmpPreviousEURegistrationDetails(
            issuedBy = "EE",
            registrationNumber = "EE123",
            schemeType = SchemeType.OSSNonUnion,
            intermediaryNumber = None
          ),
          EtmpPreviousEURegistrationDetails(
            issuedBy = "EE",
            registrationNumber = "EE234",
            schemeType = SchemeType.IOSSWithIntermediary,
            intermediaryNumber = Some("IN234")
          ),
          EtmpPreviousEURegistrationDetails(
            issuedBy = "EE",
            registrationNumber = "EE312",
            schemeType = SchemeType.IOSSWithoutIntermediary,
            intermediaryNumber = None
          )
        ),
        onlineMarketPlace = false,
        websites = Seq(
          Website(
            websiteAddress = "website1"
          ),
          Website(
            websiteAddress = "website2"
          )
        ),
        contactName = "Joe Bloggs",
        businessTelephoneNumber = "01112223344",
        businessEmailId = "email@email.com",
        nonCompliantReturns = Some("1"),
        nonCompliantPayments = Some("2"),
        exclusions = Seq.empty
      ),
      bankDetails = BankDetails("Account name", Some(bic), iban),
      adminUse = AdminUse(Some(LocalDateTime.now(stubClock)))
    )

  val optionalDisplayRegistration: EtmpDisplayRegistration =
    EtmpDisplayRegistration(
      tradingNames = Seq.empty,
      schemeDetails = EtmpDisplaySchemeDetails(
        commencementDate = LocalDate.of(2023, 1, 1).format(dateFormatter),
        None,
        None,
        None,
        euRegistrationDetails = Seq.empty,
        previousEURegistrationDetails = Seq.empty,
        onlineMarketPlace = true,
        websites = Seq.empty,
        contactName = "Mr Test",
        businessTelephoneNumber = "1234567890",
        businessEmailId = "test@testEmail.com",
        None,
        None,
        Seq.empty
      ),
      bankDetails = BankDetails(
        accountName = "Bank Account Name",
        None,
        iban
      ),
      adminUse = AdminUse(Some(LocalDateTime.now(stubClock)))
    )

  val invalidRegistration = """{"invalidName":"invalid"}"""

  def toRegistrationRequest(registration: Registration): RegistrationRequest = {
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
      registration.nonCompliantPayments,
      registration.submissionReceived,
      registration.excludedTrader
    )
  }
}
