package services

import base.BaseSpec
import org.scalatest.BeforeAndAfterEach
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._

class HistoricalRegistrationEnrolmentSpec extends BaseSpec with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    reset(registrationConnector)
    super.beforeEach()
  }



}
