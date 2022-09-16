package services.exclusions

import base.BaseSpec
import config.AppConfig
import models.Period
import models.Quarter.Q3
import models.exclusions.{ExcludedTrader, HashedExcludedTrader}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.domain.Vrn
import utils.HashingUtil

import scala.util.{Failure, Success, Try}


class ExclusionServiceSpec extends BaseSpec with BeforeAndAfterEach {

  private val mockConfig = mock[AppConfig]
  when(mockConfig.exclusionsEnabled) thenReturn true
  when(mockConfig.exclusionsHashingKey) thenReturn "mERA6vGFqQLsa4TuKmnqQTDLBQ43N8Lzbhj5auPJtHGyteuU8KCkYXFZH67sVoPa"
  private val hashingUtil = new HashingUtil(mockConfig)
  private val exclusionService = new ExclusionService(hashingUtil, mockConfig)
  private val exclusionSource = Gen.oneOf("HMRC", "TRADER").sample.value
  private val exclusionReason = Gen.oneOf("01", "02", "03", "04", "05", "06", "-01").sample.value.toInt
  private val exclusionPeriod = Period(2022, Q3)
  private val hashedVrn = hashingUtil.hashValue("123456789")

  override def beforeEach(): Unit = {
    Mockito.reset(mockConfig)
    super.beforeEach()
  }

  ".findExcludedTrader" - {

    "must return ExcludedTrader if vrn is matched" in {

      when(mockConfig.excludedTraders) thenReturn Seq(HashedExcludedTrader(hashedVrn, exclusionSource, exclusionReason, exclusionPeriod))

      val expected = ExcludedTrader(vrn, exclusionSource, exclusionReason, exclusionPeriod)

      exclusionService.findExcludedTrader(vrn).futureValue mustBe Some(expected)

    }

    "must return None if vrn is not matched" in {

      when(mockConfig.excludedTraders) thenReturn Seq.empty

      exclusionService.findExcludedTrader(vrn).futureValue mustBe None

    }

    "must return an Exception when excluded trader effective period is not parsed correctly" in {

      val exclusionService: ExclusionService = mock[ExclusionService]

      Try {
        applicationBuilder
          .overrides(bind[ExclusionService].toInstance(exclusionService))
          .configure("features.exclusions.excluded-traders.1.effectivePeriod" -> "fail")
          .build()
      } match {
        case Success(_) => fail("failed")
        case Failure(exception) =>
          exception mustBe a[Exception]
          exception.getCause.getMessage mustBe ("Unable to parse period")
      }
    }
  }

}
