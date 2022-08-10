package base

import controllers.actions.{AuthAction, FakeAuthAction}
import models.{BankDetails, Iban}
import models.etmp.{EtmpRegistrationRequest, EtmpSchemeDetails, EtmpTradingNames, Website}
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


  protected def applicationBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder().overrides(bind[AuthAction].to[FakeAuthAction])

  val dateFormatter = DateTimeFormatter.ofPattern("yyyy MM dd")
    .withLocale(Locale.UK)
    .withZone(ZoneId.of("GMT"))

  val etmpRegistrationRequest = EtmpRegistrationRequest(
    vrn,
    Seq(EtmpTradingNames("Foo")),
    EtmpSchemeDetails(
      LocalDate.now().format(dateFormatter),
      None,
      None,
      None,
      true,
      Seq(Website("www.test.com,")),
      "full name",
      "Telephone No",
      "email",
    ),
    BankDetails("test account", None, Iban("GB33BUKB20201555555555").right.get)
  )

}
