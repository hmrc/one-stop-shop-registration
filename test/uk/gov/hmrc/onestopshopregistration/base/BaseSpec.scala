package uk.gov.hmrc.onestopshopregistration.base

import akka.stream.Materializer
import com.kenshoo.play.metrics.Metrics
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{BodyParsers, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestMetrics

abstract class BaseSpec extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar with Matchers{

  override lazy val fakeApplication: Application = GuiceApplicationBuilder()
    .configure(
        "metrics.jvm"     -> false,
        "metrics.enabled" -> false
    )
    .overrides(bind[Metrics].toInstance(new TestMetrics))
    .build()

  implicit val mat: Materializer = fakeApplication.materializer

  lazy val serviceConfig: ServicesConfig     = fakeApplication.injector.instanceOf[ServicesConfig]
  lazy val parser: BodyParsers.Default       = fakeApplication.injector.instanceOf[BodyParsers.Default]
  lazy val mcc: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]

}