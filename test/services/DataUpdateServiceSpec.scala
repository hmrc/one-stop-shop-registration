package services

import base.BaseSpec
import config.AppConfig
import org.mockito.ArgumentMatchers.{any, anyInt}
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import repositories.RegistrationRepository
import utils.RegistrationData

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

      service.updateDateOfFirstSale()

      verify(registrationRepository, times(1)).get(anyInt())
      verify(registrationRepository, times(0)).updateDateOfFirstSale(any())
    }

    "must call repository.updateDateOfFirstSale once when one registration exist" in {
      val singleRegistration = RegistrationData.registration copy (dateOfFirstSale = None)

      when(registrationRepository.get(anyInt())) thenReturn Future.successful(Seq(singleRegistration))
      when(registrationRepository.updateDateOfFirstSale(any())) thenReturn Future.successful(true)

      service.updateDateOfFirstSale()

      verify(registrationRepository, times(1)).get(anyInt())
      verify(registrationRepository, times(1)).updateDateOfFirstSale(eqTo(singleRegistration))
    }
  }
}
