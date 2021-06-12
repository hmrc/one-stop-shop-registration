package utils

import models.EuTaxIdentifierType.Vat
import models.VatDetailSource.UserEntered
import models._
import uk.gov.hmrc.domain.Vrn

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
      euRegistrations = Seq(
        EuVatRegistration(Country("FR", "France"), "FR123"),
        RegistrationWithFixedEstablishment(
          Country("DE", "Germany"),
          EuTaxIdentifier(Vat, "DE123"),
          FixedEstablishment("Name", FixedEstablishmentAddress("Line 1", None, "Town", None, None))
        )
      ),
      contactDetails =     new ContactDetails(
        "Joe Bloggs",
        "01112223344",
        "email@email.com"
      ),
      websites = List("website1", "website2"),
      startDate = LocalDate.now,
      currentCountryOfRegistration = Some(Country("FR", "France")),
      previousRegistrations = Seq(
        PreviousRegistration(Country("DE", "Germany"), "DE123")
      ),
      bankDetails = BankDetails("Account name", Some("12345678"), "GB1234578")
    )

  val invalidRegistration = """{"invalidName":"invalid"}"""
}
