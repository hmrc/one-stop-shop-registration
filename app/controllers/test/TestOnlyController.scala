package controllers.test

import org.mongodb.scala.model.Filters
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.RegistrationRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TestOnlyController @Inject()(
                                    cc: ControllerComponents,
                                    registrationRepository: RegistrationRepository)(implicit ec: ExecutionContext)
  extends BackendController(cc) {

  def deleteAccounts(): Action[AnyContent] = Action.async {

    val vrnPattern = "^1110".r

    for {
      res1 <- registrationRepository.collection.deleteMany(Filters.regex("vrn", vrnPattern)).toFutureOption()
    } yield {
      Ok("Deleted Perf Tests Accounts MongoDB")
    }

  }

}