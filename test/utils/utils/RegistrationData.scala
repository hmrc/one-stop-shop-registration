package utils

import uk.gov.hmrc.onestopshopregistration.models.{BusinessAddress, BusinessContactDetails, Registration, StartDate, StartDateOption}

import java.time.LocalDate

object RegistrationData {

  def createNewRegistration(): Registration =
    Registration(
      "foo",
      true,
      Some(List("single", "double")),
      true,
      "GB123456789",
      LocalDate.now(),
      "AA1 1AA",
      true,
      Some(Map("France" -> "FR123456789", "Spain" -> "ES123456789")),
      StartDate(StartDateOption.NextPeriod, LocalDate.now()),
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
