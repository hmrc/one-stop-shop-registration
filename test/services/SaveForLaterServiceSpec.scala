package services

import generators.Generators
import models.SavedUserAnswers
import models.requests.SaveForLaterRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import repositories.SaveForLaterRepository
import uk.gov.hmrc.domain.Vrn

import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.Future


class SaveForLaterServiceSpec
  extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with ScalaCheckPropertyChecks
    with Generators
    with OptionValues
    with ScalaFutures {

  ".saveAnswers" - {

    "must create a SavedUserAnswers, attempt to save it to the repository, and respond with the result of saving" in {

      val now            = Instant.now
      val stubClock      = Clock.fixed(now, ZoneId.systemDefault())
      val answers      = arbitrary[SavedUserAnswers].sample.value
      val insertResult   = answers
      val mockRepository = mock[SaveForLaterRepository]

      when(mockRepository.set(any())) thenReturn Future.successful(insertResult)

      val request = arbitrary[SaveForLaterRequest].sample.value
      val service = new SaveForLaterService(mockRepository, stubClock)

      val result = service.saveAnswers(request).futureValue

      result mustEqual insertResult
      verify(mockRepository, times(1)).set(any())
    }
  }

  ".get" - {

    "must retrieve a sequence of Saved User Answers record" in {
      val now            = Instant.now
      val stubClock      = Clock.fixed(now, ZoneId.systemDefault())
      val answers      = arbitrary[SavedUserAnswers].sample.value
      val mockRepository = mock[SaveForLaterRepository]
      val vrn = arbitrary[Vrn].sample.value

      when(mockRepository.get(any())) thenReturn Future.successful(Some(answers))
      val service = new SaveForLaterService(mockRepository, stubClock)

      val result = service.get(vrn).futureValue
      result mustBe Some(answers)
      verify(mockRepository, times(1)).get(vrn)

    }
  }

  ".delete" - {

    "must delete a single Saved User Answers record" in {
      val now            = Instant.now
      val stubClock      = Clock.fixed(now, ZoneId.systemDefault())
      val mockRepository = mock[SaveForLaterRepository]
      val vrn = arbitrary[Vrn].sample.value

      when(mockRepository.clear(any())) thenReturn Future.successful(true)
      val service = new SaveForLaterService(mockRepository, stubClock)

      val result = service.delete(vrn).futureValue
      result mustBe true
      verify(mockRepository, times(1)).clear(vrn)

    }
  }
}

