package services

import base.BaseSpec
import config.AppConfig
import models.Registration
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyInt}
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import repositories.RegistrationRepository
import uk.gov.hmrc.domain.Vrn
import utils.RegistrationData._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataUpdateServiceSpec extends BaseSpec with BeforeAndAfterEach {

  private val registrationRepository = mock[RegistrationRepository]
  private val appConfig = mock[AppConfig]

  private val service = new DataUpdateServiceImpl(registrationRepository, appConfig)

  override def beforeEach(): Unit = {
    reset(registrationRepository)
  }

  ".updateDateOfFirstSale" - {

    when(appConfig.dbRecordLimit) thenReturn 200

    "must not call repository.updateDateOfFirstSale when no registrations exist" in {
      when(registrationRepository.get(anyInt())) thenReturn Future.successful(Seq.empty)

      val result = service.updateDateOfFirstSale().futureValue

      result mustBe Seq.empty
      verify(registrationRepository, times(1)).get(anyInt())
      verify(registrationRepository, times(0)).updateDateOfFirstSale(any())
    }

    "must call repository.updateDateOfFirstSale once when one registration exist" in {
      val singleRegistration = registration copy (dateOfFirstSale = None)
      val captor: ArgumentCaptor[Registration] = ArgumentCaptor.forClass(classOf[Registration])

      when(registrationRepository.get(anyInt())) thenReturn Future.successful(Seq(singleRegistration))
      when(registrationRepository.updateDateOfFirstSale(any())) thenReturn Future.successful(true)

      val result = service.updateDateOfFirstSale().futureValue

      result mustBe Seq(true)
      verify(registrationRepository, times(1)).get(anyInt())
      verify(registrationRepository, times(1)).updateDateOfFirstSale(captor.capture())
      captor.getValue mustBe singleRegistration
    }

    "must call repository.updateDateOfFirstSale method once when there is one registration without Date Of First Sale" in {
      val registrationWithDOFS = registration
      val registrationWithoutDOFS = registration copy (dateOfFirstSale = None)
      val captor: ArgumentCaptor[Registration] = ArgumentCaptor.forClass(classOf[Registration])

      when(
        registrationRepository.get(anyInt())
      ) thenReturn Future.successful(Seq(registrationWithDOFS, registrationWithoutDOFS))
      when(
        registrationRepository.updateDateOfFirstSale(any())
      ) thenReturn Future.successful(true)

      val result = service.updateDateOfFirstSale().futureValue

      result mustBe Seq(true)
      verify(registrationRepository, times(1)).get(anyInt())
      verify(registrationRepository, times(1)).updateDateOfFirstSale(captor.capture())
      captor.getValue mustBe registrationWithoutDOFS
    }

    "must call repository.updateDateOfFirstSale method twice when there is multiple registrations without Date Of First Sale" in {
      val registrationWithoutDOFSOne = registration copy (vrn = Vrn("000000001"), dateOfFirstSale = None)
      val registrationWithoutDOFSTwo = registration copy (vrn = Vrn("000000002"), dateOfFirstSale = None)
      val captor: ArgumentCaptor[Registration] = ArgumentCaptor.forClass(classOf[Registration])

      when(registrationRepository.get(anyInt())) thenReturn Future.successful(Seq(registrationWithoutDOFSOne, registrationWithoutDOFSTwo))
      when(registrationRepository.updateDateOfFirstSale(any())) thenReturn Future.successful(true)

      val result = service.updateDateOfFirstSale().futureValue

      result mustBe Seq(true, true)
      verify(registrationRepository, times(1)).get(anyInt())
      verify(registrationRepository, times(2)).updateDateOfFirstSale(captor.capture())
      captor.getAllValues.get(0) mustBe registrationWithoutDOFSOne
      captor.getAllValues.get(1) mustBe registrationWithoutDOFSTwo
    }
  }
}
