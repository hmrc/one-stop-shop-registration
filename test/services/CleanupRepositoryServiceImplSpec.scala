package services

import base.BaseSpec
import config.AppConfig
import connectors.EnrolmentsConnector
import models.enrolments.HistoricTraderForEnrolment
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mongodb.scala.{MongoCollection, MongoDatabase, Observable, SingleObservable}
import org.mongodb.scala.bson.collection.immutable.Document
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.IntegrationPatience
import play.api.http.Status.CREATED
import testutils.RegistrationData.registration
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.mongo.MongoComponent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CleanupRepositoryServiceImplSpec extends BaseSpec with BeforeAndAfterEach with IntegrationPatience {

  private val mockMongoComponent = mock[MongoComponent]
  private val mockAppConfig = mock[AppConfig]

  override def beforeEach(): Unit = {
    reset(mockMongoComponent)
    reset(mockAppConfig)
    super.beforeEach()
  }

  private val service = new CleanupRepositoryServiceImpl(
    mockMongoComponent,
    mockAppConfig
  )

  "startCleanup()" - {
    "when enabled" - {
      "must drop collections for multiple collections found" in {
        val collections = Seq("collection1", "collection2")
        when(mockAppConfig.cleanupOldCollectionsEnabled) thenReturn true
        when(mockAppConfig.cleanupOldCollectionsList) thenReturn collections
        val mockDatabase = mock[MongoDatabase]
        when(mockMongoComponent.database) thenReturn mockDatabase
        when(mockDatabase.listCollectionNames()) thenReturn Observable(collections)
        val mockCollection = mock[MongoCollection[Nothing]]
        when(mockDatabase.getCollection(any())(any(), any())) thenReturn mockCollection
        val mockSingleObservableVoid = mock[SingleObservable[Void]]
        when(mockCollection.drop()) thenReturn mockSingleObservableVoid
        when(mockSingleObservableVoid.toFuture()) thenReturn Future.successful(null.asInstanceOf[Void])

        service.trigger().futureValue.size mustBe 2

        verify(mockCollection, times(2)).drop()
      }

      "must not drop collections when no collections found" in {
        val collections = Seq("collection1", "collection2")
        when(mockAppConfig.cleanupOldCollectionsEnabled) thenReturn true
        when(mockAppConfig.cleanupOldCollectionsList) thenReturn collections
        val mockDatabase = mock[MongoDatabase]
        when(mockMongoComponent.database) thenReturn mockDatabase
        when(mockDatabase.listCollectionNames()) thenReturn Observable(Seq.empty)
        val mockCollection = mock[MongoCollection[Nothing]]
        when(mockDatabase.getCollection(any())(any(), any())) thenReturn mockCollection

        service.trigger().futureValue.size mustBe 0

        verifyNoInteractions(mockCollection)
      }

    }

    "when not enabled" - {
      "does nothing" in {
        when(mockAppConfig.cleanupOldCollectionsEnabled) thenReturn false

        service.trigger().futureValue.size mustBe 0

        verifyNoInteractions(mockMongoComponent)
      }
    }
  }
}
