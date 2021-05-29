package utils

import uk.gov.hmrc.onestopshopregistration.models._

import java.time.LocalDate

object RegistrationData {

  def createNewRegistration(): Registration =
    Registration(
      "foo",
      true,
      List("single", "double"),
      true,
      "GB123456789",
      LocalDate.now(),
      "AA1 1AA",
      true,
      List(EuVatDetails(Country("FR", "France"), "FR123")),
      LocalDate.now,
      createBusinessAddress(),
      List("website1", "website2"),
      createBusinessContactDetails()
    )

  def createInvalidRegistration() = """{"invalidName":"invalid"}"""

  private def createBusinessAddress(): BusinessAddress =
    BusinessAddress(
      "123 Street",
      Some("Street"),
      "City",
      Some("county"),
      "AA12 1AB"
    )

  private def createBusinessContactDetails(): BusinessContactDetails =
    BusinessContactDetails(
      "Joe Bloggs",
      "01112223344",
      "email@email.com"
    )
}
