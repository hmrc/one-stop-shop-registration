/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.test

import models.EuTaxIdentifierType.Vat
import models.VatDetailSource.UserEntered
import models.{BankDetails, ContactDetails, Country, DesAddress, EuTaxIdentifier, TradeDetails, Iban, InternationalAddress, PreviousRegistration, PrincipalPlaceOfBusinessInNi, Registration, RegistrationWithFixedEstablishment, RegistrationWithoutFixedEstablishment, VatDetails}
import org.mongodb.scala.model.Filters
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.RegistrationRepository
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.{Clock, Instant, LocalDate}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TestOnlyController @Inject()(
                                    cc: ControllerComponents,
                                    clock: Clock,
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

  def createRegistrations(startVrn:Int, endVrn: Int, commencementDate: LocalDate): Action[AnyContent] = Action.async {

    val vrns = startVrn to endVrn toList

    val regitrations = vrns.map(vrn =>
      registration.copy(vrn = Vrn(vrn.toString), commencementDate = commencementDate)
    )
    for {
      res1 <- registrationRepository.insertMany(regitrations)
    } yield {
      Ok(s"Inserted Perf Tests Registrations for vrns $startVrn to $endVrn and commencementDate $commencementDate MongoDB")
    }

  }

  private val iban: Iban = Iban("GB33BUKB20201555555555").right.get

  private val registration: Registration =
    Registration(
      vrn = Vrn("123456789"),
      registeredCompanyName = "foo",
      tradingNames = List("single", "double"),
      vatDetails = VatDetails(
        registrationDate = LocalDate.now,
        address = DesAddress(
          "123 Street",
          Some("Street"),
          Some("City"),
          Some("county"),
          None,
          Some("AA12 1AB"),
          "GB",
        ),
        partOfVatGroup = true,
        source = UserEntered
      ),
      euRegistrations = Seq(
        RegistrationWithoutFixedEstablishment(
          Country("FR", "France"),
          EuTaxIdentifier(Vat, "FR123"),
          Some(false),
          None,
          None
        ),
        RegistrationWithFixedEstablishment(
          Country("DE", "Germany"),
          EuTaxIdentifier(Vat, "DE123"),
          TradeDetails("Name", InternationalAddress("Line 1", None, "Town", None, None, Country("FR", "France")))
        )
      ),
      contactDetails =     new ContactDetails(
        "Joe Bloggs",
        "01112223344",
        "email@email.com"
      ),
      websites = List("website1", "website2"),
      commencementDate = LocalDate.now,
      previousRegistrations = Seq(
        PreviousRegistration(Country("DE", "Germany"), "DE123")
      ),
      bankDetails = BankDetails("Account name", None, iban),
      isOnlineMarketplace = false,
      niPresence = Some(PrincipalPlaceOfBusinessInNi),
      dateOfFirstSale = Some(LocalDate.now),
      submissionReceived = Instant.now(clock),
      lastUpdated = Instant.now(clock),
      nonCompliantReturns =  Some(1),
      nonCompliantPayments = Some(2)
    )

}