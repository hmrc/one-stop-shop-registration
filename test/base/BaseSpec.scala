package base

import controllers.actions.{AuthAction, FakeAuthAction}
import generators.Generators
import models.Quarter.Q3
import models._
import models.amend.{EtmpAmendRegistrationRequest, EtmpExclusionDetails, EtmpRequestedChange, EtmpSelfExclusionReason}
import models.des.VatCustomerInfo
import models.etmp._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.{FakeHistoricalRegistrationEnrolmentService, HistoricalRegistrationEnrolmentService}
import uk.gov.hmrc.domain.Vrn

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate, ZoneId}
import java.util.Locale

trait BaseSpec
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with EitherValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with Generators {

  protected val vrn: Vrn = Vrn("123456789")

  val stubClock: Clock = Clock.fixed(LocalDate.now.atStartOfDay(ZoneId.systemDefault).toInstant, ZoneId.systemDefault)
  val iban: Iban = Iban("GB33BUKB20201555555555").toOption.get
  val bic: Bic = Bic("ABCDGB2A").get

  val period: Period = StandardPeriod(2021, Q3)

  protected def applicationBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(bind[AuthAction].to[FakeAuthAction])
      .overrides(bind[HistoricalRegistrationEnrolmentService].to[FakeHistoricalRegistrationEnrolmentService])

  val userId: String = "12345-userId"


  val vatCustomerInfo: VatCustomerInfo =
    VatCustomerInfo(
      registrationDate = Some(LocalDate.now(stubClock)),
      address = DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
      partOfVatGroup = false,
      organisationName = Some("Company name"),
      singleMarketIndicator = Some(true),
      individualName = None,
      deregistrationDecisionDate = Some(LocalDate.now(stubClock))
    )

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    .withLocale(Locale.UK)
    .withZone(ZoneId.of("GMT"))

  val etmpRegistrationRequest: EtmpRegistrationRequest = EtmpRegistrationRequest(
    administration = EtmpAdministration(EtmpMessageType.OSSSubscriptionCreate),
    customerIdentification = EtmpCustomerIdentification(vrn),
    tradingNames = Seq(EtmpTradingNames("Foo")),
    schemeDetails = EtmpSchemeDetails(
      commencementDate = LocalDate.now().format(dateFormatter),
      firstSaleDate = Some(LocalDate.now().format(dateFormatter)),
      euRegistrationDetails = Seq(EtmpEuRegistrationDetails(
        countryOfRegistration = "FR"
      ),
        EtmpEuRegistrationDetails(
          countryOfRegistration = "DE",
          vatNumber = Some("DE123"),
          taxIdentificationNumber = None,
          fixedEstablishment = Some(true),
          tradingName = Some("Name"),
          fixedEstablishmentAddressLine1 = Some("Line 1"),
          townOrCity = Some("Town")
        ),
        EtmpEuRegistrationDetails(
          countryOfRegistration = "BE",
          taxIdentificationNumber = Some("12345"),
          fixedEstablishment = Some(false),
          tradingName = Some("Name"),
          fixedEstablishmentAddressLine1 = Some("Line 1"),
          fixedEstablishmentAddressLine2 = Some("Line 2"),
          townOrCity = Some("Town"),
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
        Website("website1"), Website("website2")
      ),
      contactName = "Joe Bloggs",
      businessTelephoneNumber = "01112223344",
      businessEmailId = "email@email.com",
      nonCompliantReturns = Some("1"),
      nonCompliantPayments = Some("2")
    ),
    BankDetails("Account name", Some(bic), Iban("GB33BUKB20201555555555").toOption.get)
  )

  val etmpAmendRegistrationRequest: EtmpAmendRegistrationRequest = EtmpAmendRegistrationRequest(
    administration = EtmpAdministration(EtmpMessageType.OSSSubscriptionAmend),
    customerIdentification = EtmpCustomerIdentification(vrn),
    tradingNames = Seq(EtmpTradingNames("Foo")),
    schemeDetails = EtmpSchemeDetails(
      commencementDate = LocalDate.now().format(dateFormatter),
      firstSaleDate = Some(LocalDate.now().format(dateFormatter)),
      euRegistrationDetails = Seq(EtmpEuRegistrationDetails(
        countryOfRegistration = "FR"
      ),
        EtmpEuRegistrationDetails(
          countryOfRegistration = "DE",
          vatNumber = Some("DE123"),
          taxIdentificationNumber = None,
          fixedEstablishment = Some(true),
          tradingName = Some("Name"),
          fixedEstablishmentAddressLine1 = Some("Line 1"),
          townOrCity = Some("Town")
        ),
        EtmpEuRegistrationDetails(
          countryOfRegistration = "BE",
          taxIdentificationNumber = Some("12345"),
          fixedEstablishment = Some(false),
          tradingName = Some("Name"),
          fixedEstablishmentAddressLine1 = Some("Line 1"),
          fixedEstablishmentAddressLine2 = Some("Line 2"),
          townOrCity = Some("Town"),
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
        Website("website1"), Website("website2")
      ),
      contactName = "Joe Bloggs",
      businessTelephoneNumber = "01112223344",
      businessEmailId = "email@email.com",
      nonCompliantReturns = Some("1"),
      nonCompliantPayments = Some("2")
    ),
    BankDetails("Account Foo", Some(bic), Iban("GB33BUKB20201555555555").toOption.get),
    requestedChange = EtmpRequestedChange(
      tradingName = true,
      fixedEstablishment = false,
      contactDetails = false,
      bankDetails = true,
      reRegistration = false,
      exclusion = true
    ),
    exclusionDetails = Some(EtmpExclusionDetails(
      exclusionRequestDate = LocalDate.now(),
      exclusionReason = EtmpSelfExclusionReason.NoLongerSupplies,
      movePOBDate = None,
      issuedBy = None,
      vatNumber = None
    ))
  )

}


