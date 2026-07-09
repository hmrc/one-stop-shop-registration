package repositories

import com.typesafe.config.Config
import config.AppConfig
import crypto.CachedRegistrationEncryptor
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

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
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
  private val mockEncryptionService: EncryptionService = mock[EncryptionService]
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

  private val encryptedCachedRegistrationWrapper = "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"

  when(mockEncryptionService.encryptField(any())) thenReturn encryptedCachedRegistrationWrapper
  when(mockEncryptionService.decryptField(any())) thenReturn Json.toJson(registration).toString

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
