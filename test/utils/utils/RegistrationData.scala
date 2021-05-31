package utils

import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.onestopshopregistration.models._

import java.time.LocalDate

object RegistrationData {

  val registration: Registration =
    Registration(
      vrn = Vrn("123456789"),
      registeredCompanyName = "foo",
      tradingNames = List("single", "double"),
      partOfVatGroup = true,
      vatEffectiveDate = LocalDate.now(),
      vatRegisteredPostcode = "AA1 1AA",
      euVatRegistrations = Seq(EuVatRegistration(Country("FR", "France"), "FR123", None)),
      businessAddress = new Address(
        "123 Street",
        Some("Street"),
        "City",
        Some("county"),
        "AA12 1AB"
      ),
      contactDetails =     new BusinessContactDetails(
        "Joe Bloggs",
        "01112223344",
        "email@email.com"
      ),
      websites = List("website1", "website2"),
      startDate = LocalDate.now,
      currentCountryOfRegistration = Some(Country("FR", "France"))
    )

  val invalidRegistration = """{"invalidName":"invalid"}"""
}
