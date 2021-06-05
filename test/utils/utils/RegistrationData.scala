package utils

import models.VatDetailSource.UserEntered
import uk.gov.hmrc.domain.Vrn
import models._
import models.des.DesAddress

import java.time.LocalDate

object RegistrationData {

  val registration: Registration =
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
      euVatRegistrations = Seq(EuVatRegistration(Country("FR", "France"), "FR123", None)),
      contactDetails =     new BusinessContactDetails(
        "Joe Bloggs",
        "01112223344",
        "email@email.com"
      ),
      websites = List("website1", "website2"),
      startDate = LocalDate.now,
      currentCountryOfRegistration = Some(Country("FR", "France")),
      previousRegistrations = Seq(
        PreviousRegistration(Country("DE", "Germany"), "DE123")
      )
    )

  val invalidRegistration = """{"invalidName":"invalid"}"""
}
