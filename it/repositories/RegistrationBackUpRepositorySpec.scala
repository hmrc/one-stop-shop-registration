package repositories

import config.AppConfig
import crypto.{RegistrationEncrypter, SecureGCMCipher}
import models.EncryptedRegistration
import org.mockito.MockitoSugar
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.mongo.test.{CleanMongoCollectionSupport, DefaultPlayMongoRepositorySupport}
import utils.RegistrationData

import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationBackUpRepositorySpec extends AnyFreeSpec
  with Matchers
  with DefaultPlayMongoRepositorySupport[EncryptedRegistration]
  with CleanMongoCollectionSupport
  with ScalaFutures
  with IntegrationPatience
  with OptionValues
  with MockitoSugar {

  private val cipher    = new SecureGCMCipher
  private val encrypter = new RegistrationEncrypter(cipher)
  private val appConfig = mock[AppConfig]
  private val encryptionKey = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="

  when(appConfig.encryptionKey) thenReturn encryptionKey

  override protected val repository = new RegistrationBackUpRepository(
    mongoComponent = mongoComponent,
  )

  ".insertMany" - {

    "must return true if given an empty list" in {
      val emptyList = List.empty
      val result = repository.insertMany(emptyList).futureValue

      result mustBe true
    }

    "must return true if given a list of Encrypted Registrations and insertMany succeed" in {
      val registrationOne = RegistrationData.registration copy (vrn = Vrn("643812347"))
      val registrationTwo = RegistrationData.registration copy (vrn = Vrn("537290347"))
      val encryptedRegistrationOne = encrypter.encryptRegistration(registrationOne, registrationOne.vrn, encryptionKey)
      val encryptedRegistrationTwo = encrypter.encryptRegistration(registrationTwo, registrationTwo.vrn, encryptionKey)

      val encryptedRegistrations = List(encryptedRegistrationOne, encryptedRegistrationTwo)

      val result = repository.insertMany(encryptedRegistrations).futureValue

      result mustBe true
      findAll().futureValue mustEqual encryptedRegistrations
    }

    "must return false if given a list of Encrypted Registrations when insertMany failed" in {
      val registrationOne = RegistrationData.registration copy (vrn = Vrn("435432347"))
      val encryptedRegistrationOne = encrypter.encryptRegistration(registrationOne, registrationOne.vrn, encryptionKey)

      val encryptedRegistrations = List(encryptedRegistrationOne, encryptedRegistrationOne)

      val result = repository.insertMany(encryptedRegistrations).futureValue

      result mustBe false
    }





  }


}
