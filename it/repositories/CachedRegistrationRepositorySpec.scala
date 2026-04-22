package repositories

import com.typesafe.config.Config
import config.AppConfig
import crypto.{AesGCMCrypto, CachedRegistrationEncryptor}
import generators.Generators
import models.{CachedRegistrationWrapper, EncryptedCachedRegistrationWrapper, Registration}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mongodb.scala.model.Filters
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Configuration
import play.api.libs.json.Json
import services.crypto.EncryptionService
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Clock, Instant, ZoneId}
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global

class CachedRegistrationRepositorySpec
  extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[EncryptedCachedRegistrationWrapper]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar
    with Generators {

  private val userId  = "id-123"
  private val vrn  = Vrn("123456789")
  private val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)
  private val registration: Registration = arbitrary[Registration].sample.value.copy(vrn = vrn)
  private val mockConfiguration = mock[Configuration]
  private val mockConfig = mock[Config]
  private val mockAppConfig = mock[AppConfig]
  private val mockEncryptionService: EncryptionService = new EncryptionService(mockConfiguration)
  private val encryptor = new CachedRegistrationEncryptor(mockAppConfig, mockEncryptionService)
  private val secretKey: String = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="

  when(mockAppConfig.cacheTtl) thenReturn 1L

  protected override val repository: CachedRegistrationRepository = new CachedRegistrationRepository(
    mongoComponent = mongoComponent,
    encryptor = encryptor,
    appConfig = mockAppConfig,
    clock = stubClock,
  )

  when(mockConfiguration.underlying) thenReturn mockConfig
  when(mockConfig.getString(any())) thenReturn secretKey
  when(mockAppConfig.encryptionKey) thenReturn secretKey

  ".set" - {

    "must set the last updated time to `now` and save the registration" in {

      val expectedResult = encryptor.encryptRegistration(CachedRegistrationWrapper(userId, Some(registration), Instant.now(stubClock).truncatedTo(ChronoUnit.MILLIS)), vrn)

      val setResult = repository.set(userId, vrn, Some(registration)).futureValue
      val dbRecord  = find(Filters.equal("_id", userId)).futureValue.headOption.value

      setResult mustEqual true
      dbRecord mustEqual expectedResult
    }
  }

  ".get" - {

    "when there is a record for this user" - {

      "must get the record" in {

        val wrapper = CachedRegistrationWrapper(userId, Some(registration), Instant.now(stubClock).truncatedTo(ChronoUnit.MILLIS))

        insert(encryptor.encryptRegistration(wrapper, vrn)).futureValue

        val result = repository.get(userId, vrn).futureValue

        result.value mustEqual wrapper
      }
    }

    "when there is no record for this user" - {

      "must return None" in {

        repository.get(userId, vrn).futureValue must not be defined
      }
    }
  }
}
