package repositories

import config.AppConfig
import generators.Generators
import models.Registration
import org.mockito.Mockito.when
import org.mongodb.scala.model.Filters
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Clock, Instant, ZoneId}
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global

class CachedRegistrationRepositorySpec
  extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[RegistrationWrapper]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar
    with Generators {

  private val userId  = "id-123"
  private val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)
  private val registration: Registration = arbitrary[Registration].sample.value

  private val mockAppConfig = mock[AppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1L

  protected override val repository: CachedRegistrationRepository = new CachedRegistrationRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock
  )

  ".set" - {

    "must set the last updated time to `now` and save the registration" in {

      val expectedResult = RegistrationWrapper(userId, Some(registration), Instant.now(stubClock).truncatedTo(ChronoUnit.MILLIS))

      val setResult = repository.set(userId, Some(registration)).futureValue
      val dbRecord  = find(Filters.equal("_id", userId)).futureValue.headOption.value

      setResult mustEqual true
      dbRecord mustEqual expectedResult
    }
  }

  ".get" - {

    "when there is a record for this user" - {

      "must get the record" in {

        val wrapper = RegistrationWrapper(userId, Some(registration), Instant.now(stubClock).truncatedTo(ChronoUnit.MILLIS))
        insert(wrapper).futureValue

        val result = repository.get(userId).futureValue

        result.value mustEqual wrapper
      }
    }

    "when there is no record for this user" - {

      "must return None" in {

        repository.get(userId).futureValue must not be defined
      }
    }
  }
}
