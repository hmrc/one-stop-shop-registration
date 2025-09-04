package services.cron

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Level, Logger, LoggerContext}
import ch.qos.logback.core.AppenderBase
import com.mongodb.client.result.UpdateResult
import connectors.RegistrationConnector
import models.RegistrationStatus
import models.etmp.EtmpRegistrationStatus
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{atLeastOnce, reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import org.slf4j.LoggerFactory
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.*
import play.api.test.Helpers.running
import repositories.RegistrationStatusRepository
import services.HistoricalRegistrationEnrolmentService

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}


class CronServiceSpec
  extends AnyFreeSpec
    with MockitoSugar
    with Matchers
    with GuiceOneAppPerTest
    with BeforeAndAfterEach {

  protected val ec: ExecutionContext = ExecutionContext.Implicits.global
  protected val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]
  protected val mockHistoricalRegistrationEnrolmentService: HistoricalRegistrationEnrolmentService = mock[HistoricalRegistrationEnrolmentService]
  protected val mockRegistrationStatusRepository: RegistrationStatusRepository = mock[RegistrationStatusRepository]

  protected val mockedUpdateResult: UpdateResult = UpdateResult.acknowledged(1, 1, null)
  protected val result: Seq[(RegistrationStatus, UpdateResult)] = Seq((makeRegistrationStatus("1"), mockedUpdateResult))


  def makeRegistrationStatus(id: String): RegistrationStatus = {
    new RegistrationStatus(
      subscriptionId = id,
      status = EtmpRegistrationStatus.Pending,
      lastUpdated = Instant.now())
  }


  override def beforeEach(): Unit = {
    reset(mockRegistrationConnector)
    reset(mockHistoricalRegistrationEnrolmentService)
    reset(mockRegistrationStatusRepository)
    super.beforeEach()
  }

  ".CronServiceImpl" - {

    "should run once on startup when the feature switch is true" in {

      when(mockRegistrationStatusRepository.fixAllDocuments()).thenReturn(Future.successful(result))

      val app = new GuiceApplicationBuilder()
        .configure("features.delay" -> 1, "features.enableLastUpdatedDatabaseChange" -> true)
        .overrides(bind[RegistrationConnector].to(mockRegistrationConnector))
        .overrides(bind[HistoricalRegistrationEnrolmentService].to(mockHistoricalRegistrationEnrolmentService))
        .overrides(bind[RegistrationStatusRepository].to(mockRegistrationStatusRepository))
        .build()

      running(app) {
        val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
        val serviceLogger: Logger = context.getLogger("application.services.cron.CronServiceImpl")

        val appender = new InMemoryAppender()
        appender.start()
        serviceLogger.addAppender(appender)
        serviceLogger.setLevel(Level.INFO)

        Thread.sleep(8000)

          appender.messages.head mustBe "Implementing TTL: 1 documents were read as last updated Instant.now and set to current date & time."
          verify(mockRegistrationStatusRepository, times(1)).fixAllDocuments(any())
          serviceLogger.detachAppender(appender)

      }
    }

    "should not run and log when the feature switch is false" in {

      val app = new GuiceApplicationBuilder()
        .configure("features.delay" -> 1, "features.enableLastUpdatedDatabaseChange" -> false)
        .overrides(bind[RegistrationConnector].to(mockRegistrationConnector))
        .overrides(bind[HistoricalRegistrationEnrolmentService].to(mockHistoricalRegistrationEnrolmentService))
        .overrides(bind[RegistrationStatusRepository].to(mockRegistrationStatusRepository))
        .build()

      running(app) {
        val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
        val serviceLogger: Logger = context.getLogger("application.services.cron.CronServiceImpl")

        val appender = new InMemoryAppender()
        appender.start()
        serviceLogger.addAppender(appender)
        serviceLogger.setLevel(Level.INFO)

        Thread.sleep(8000)

          appender.messages.head mustBe "ExpiryScheduler disabled; not starting."
          verify(mockRegistrationStatusRepository, times(0)).fixAllDocuments()
          serviceLogger.detachAppender(appender)
      }
    }
  }
}

class InMemoryAppender extends AppenderBase[ILoggingEvent] {
  val events = new scala.collection.mutable.ListBuffer[ILoggingEvent]()

  override def append(event: ILoggingEvent): Unit = events += event

  def messages: Seq[String] = events.map(_.getFormattedMessage).toSeq
}