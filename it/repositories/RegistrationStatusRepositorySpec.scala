package repositories

import config.AppConfig
import models.RegistrationStatus
import models.etmp.EtmpRegistrationStatus
import models.repository.InsertResult.{AlreadyExists, InsertSucceeded}
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.mongo.test.{CleanMongoCollectionSupport, PlayMongoRepositorySupport}

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*

class RegistrationStatusRepositorySpec extends AnyFreeSpec
  with Matchers
  with CleanMongoCollectionSupport
  with PlayMongoRepositorySupport[RegistrationStatus]
  with ScalaFutures
  with IntegrationPatience
  with OptionValues
  with MockitoSugar {

  private val appConfig = mock[AppConfig]
  protected val repository: RegistrationStatusRepository =
    new RegistrationStatusRepository(
      mongoComponent = mongoComponent,
      appConfig = appConfig
    )

  val registrationStatus: RegistrationStatus = RegistrationStatus("100000001-id",
    EtmpRegistrationStatus.Success, Instant.now.truncatedTo(ChronoUnit.MILLIS))

  ".insert" - {

    "must insert data" in {

      val insertResult = repository.insert(registrationStatus).futureValue
      val databaseRecord = findAll().futureValue.headOption.value

      insertResult mustEqual InsertSucceeded
      databaseRecord mustEqual registrationStatus
    }

    "must not allow duplicate data to be inserted" in {

      repository.insert(registrationStatus).futureValue
      val secondResult = repository.insert(registrationStatus).futureValue

      secondResult mustEqual AlreadyExists
    }
  }

  ".set registration status" - {

    "must update the existing status" in {

      repository.insert(registrationStatus).futureValue
      val updatedRegistrationStatus = registrationStatus.copy(status = EtmpRegistrationStatus.Pending)

      val updatedResult = repository.set(updatedRegistrationStatus).futureValue

      updatedResult mustEqual updatedRegistrationStatus
    }
  }

  ".get one" - {

    "must return Saved record when one exists for this subscription id" in {

      insert(registrationStatus).futureValue

      val result = repository.get(registrationStatus.subscriptionId).futureValue

      result.value mustEqual registrationStatus
    }

    "must return None when no data exists" in {

      val result = repository.get("100000002-id").futureValue

      result must not be defined
    }
  }

  ".delete" - {

    "must return true when saved record is deleted" in {

      insert(registrationStatus).futureValue

      val result = repository.delete(registrationStatus.subscriptionId).futureValue

      result mustEqual true
    }
  }
  
  ".fixAllDocuments" - {

    "must find and set all records with a lastUpdated now (factoring in potential computation time)" in {

      insert(registrationStatus).futureValue

      val result = repository.fixAllDocuments().futureValue

      result.size mustEqual 1
    }
    
    "must return no record if none LastUpdated now" in {
      
      val futureRegistrationStatus5Hours: RegistrationStatus = registrationStatus.copy(subscriptionId = "uniqueId1", lastUpdated = Instant.now().minus(5, ChronoUnit.HOURS))
      val futureRegistrationStatus7Hours: RegistrationStatus = registrationStatus.copy(subscriptionId = "uniqueId2", lastUpdated = Instant.now().minus(7, ChronoUnit.HOURS))
      val futureRegistrationStatus9Hours: RegistrationStatus = registrationStatus.copy(subscriptionId = "uniqueId3", lastUpdated = Instant.now().minus(9, ChronoUnit.HOURS))
      
      insert(futureRegistrationStatus5Hours).futureValue
      insert(futureRegistrationStatus7Hours).futureValue
      insert(futureRegistrationStatus9Hours).futureValue

      val result = repository.fixAllDocuments().futureValue

      result.size mustEqual 0
    }
    "must return and set all records that have lastUpdated as Instant.now() (factoring in potential computation time)" in {
      
      val registrationStatusNow: RegistrationStatus = registrationStatus.copy(subscriptionId = "uniqueId1")
      val registrationStatusNow2: RegistrationStatus = registrationStatus.copy(subscriptionId = "uniqueId2")
      val registrationStatusRecent: RegistrationStatus = registrationStatus.copy(subscriptionId = "uniqueId3", lastUpdated = Instant.now().minus(10, ChronoUnit.MILLIS))
      val registrationStatusEarlier: RegistrationStatus = registrationStatus.copy(subscriptionId = "uniqueId4", lastUpdated = Instant.now().minus(10, ChronoUnit.SECONDS))

      insert(registrationStatusNow).futureValue
      insert(registrationStatusNow2).futureValue
      insert(registrationStatusRecent).futureValue
      insert(registrationStatusEarlier).futureValue

      val roundedNow = Instant.now().truncatedTo(ChronoUnit.SECONDS)
      val result = repository.fixAllDocuments().futureValue

      result.size mustEqual 3
      result.foreach{result =>
      result._1.lastUpdated.truncatedTo(ChronoUnit.SECONDS) mustEqual roundedNow
      result._2.wasAcknowledged() mustBe true
      }
    }
  }
}
