package base

import controllers.actions.{AuthAction, FakeAuthAction}
import models.Quarter.Q3
import models.etmp._
import models.{BankDetails, Bic, Iban, Period}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Vrn

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate, ZoneId}
import java.util.Locale

trait BaseSpec
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar {

  protected val vrn: Vrn = Vrn("123456789")

  val stubClock: Clock = Clock.fixed(LocalDate.now.atStartOfDay(ZoneId.systemDefault).toInstant, ZoneId.systemDefault)
  val iban: Iban = Iban("GB33BUKB20201555555555").right.get
  val bic: Bic = Bic("ABCDGB2A").get

  val period: Period = Period(2021, Q3)

  protected def applicationBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder().overrides(bind[AuthAction].to[FakeAuthAction])

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy MM dd")
    .withLocale(Locale.UK)
    .withZone(ZoneId.of("GMT"))

  val etmpRegistrationRequest: EtmpRegistrationRequest = EtmpRegistrationRequest(
    vrn = vrn,
    tradingNames = Seq(EtmpTradingNames("Foo")),
    schemeDetails = EtmpSchemeDetails(
      commencementDate = LocalDate.now().format(dateFormatter),
      firstSaleDate = Some(LocalDate.now().format(dateFormatter)),
      euRegistrationDetails = Seq(EtmpEuRegistrationDetails(
        countryOfRegistration = "FR",
        vatNumber = None,
        taxIdentificationNumber = Some("FR123"),
        fixedEstablishment = None,
        tradingName = None,
        fixedEstablishmentAddressLine1 = None,
        fixedEstablishmentAddressLine2 = None,
        townOrCity = None,
        regionOrState = None,
        postcode = None
      ),
        EtmpEuRegistrationDetails(
          countryOfRegistration = "DE",
          vatNumber = None,
          taxIdentificationNumber = Some("DE123"),
          fixedEstablishment = Some(true),
          tradingName = Some("Name"),
          fixedEstablishmentAddressLine1 = Some("Line 1"),
          fixedEstablishmentAddressLine2 = None,
          townOrCity = Some("Town"),
          regionOrState = None,
          postcode = None)
      ),
      previousEURegistrationDetails = Seq(EtmpPreviousEURegistrationDetails(
        issuedBy = "DE",
        registrationNumber = "DE123",
        schemeType = SchemeType.OSSUnion,
        intermediaryNumber = None
      )),
      onlineMarketPlace = false,
      websites = Seq(
        Website("website1"), Website("website2")
      ),
      contactName = "Joe Bloggs",
      businessTelephoneNumber = "01112223344",
      businessEmailId = "email@email.com",
      nonCompliantReturns = Some(1),
      nonCompliantPayments = Some(2)
    ),
    BankDetails("Account name", Some(bic), Iban("GB33BUKB20201555555555").right.get)
  )

}


