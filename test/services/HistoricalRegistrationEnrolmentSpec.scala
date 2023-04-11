package services

import base.BaseSpec
import config.AppConfig
import connectors.EnrolmentsConnector
import models.enrolments.HistoricTraderForEnrolment
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.IntegrationPatience
import play.api.http.Status.CREATED
import testutils.RegistrationData.registration
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HistoricalRegistrationEnrolmentSpec extends BaseSpec with BeforeAndAfterEach with IntegrationPatience {

  private val mockEnrolmentsConnector = mock[EnrolmentsConnector]
  private val mockRegistrationService = mock[RegistrationService]
  private val mockAppConfig = mock[AppConfig]

  // TODO generators
  private val historicTraderForEnrolment1 = HistoricTraderForEnrolment(Vrn("123456789"), "groupId1", "userId1")
  private val historicTraderForEnrolment2 = HistoricTraderForEnrolment(Vrn("987654321"), "groupId2", "userId2")

  override def beforeEach(): Unit = {
    reset(mockEnrolmentsConnector)
    reset(mockRegistrationService)
    reset(mockAppConfig)
    super.beforeEach()
  }

  private val service = new HistoricalRegistrationEnrolmentServiceImpl(
    mockAppConfig,
    mockEnrolmentsConnector,
    mockRegistrationService,
    stubClock)

  "sendEnrolmentForUsers()" - {
    "when enabled" - {
      "must submit enrolments for multiple users" in {
        when(mockAppConfig.historicTradersForEnrolmentEnabled) thenReturn true
        when(mockAppConfig.historicTradersForEnrolment) thenReturn Seq(historicTraderForEnrolment1, historicTraderForEnrolment2)
        when(mockRegistrationService.get(any())) thenReturn Future.successful(Some(registration))
        when(mockEnrolmentsConnector.es8(any(), any(), any(), any())) thenReturn Future(HttpResponse(CREATED, ""))

        service.sendEnrolmentForUsers().futureValue mustBe true

        verify(mockEnrolmentsConnector, times(2)).es8(any(), any(), any(), any())
      }

      "must stop upon one failure" in {
        when(mockAppConfig.historicTradersForEnrolmentEnabled) thenReturn true
        when(mockAppConfig.historicTradersForEnrolment) thenReturn Seq(historicTraderForEnrolment1, historicTraderForEnrolment2)
        when(mockRegistrationService.get(any())) thenReturn Future.successful(Some(registration))
        when(mockEnrolmentsConnector.es8(any(), any(), any(), any())) thenReturn Future(HttpResponse(500, "error"))

        service.sendEnrolmentForUsers().futureValue mustBe true

        verify(mockEnrolmentsConnector, times(1)).es8(any(), any(), any(), any())
      }
    }

    "when not enabled" - {
      "does nothing" in {
        when(mockAppConfig.historicTradersForEnrolmentEnabled) thenReturn false

        service.sendEnrolmentForUsers().futureValue mustBe true

        verifyNoInteractions(mockEnrolmentsConnector)
      }
    }
  }


}
