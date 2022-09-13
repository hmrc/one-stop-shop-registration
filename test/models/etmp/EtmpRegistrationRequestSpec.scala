package models.etmp

import base.BaseSpec
import models.EuTaxIdentifierType.Vat
import models.VatDetailSource.UserEntered
import models.requests.RegistrationRequest
import models._
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate

class EtmpRegistrationRequestSpec extends BaseSpec {

  "EtmpRegistrationRequest" - {

    ".fromRegistrationRequest" - {

      "should return a correctly mapped Etmp Registration Request when invoked" in {

        val registrationRequest: RegistrationRequest =
          RegistrationRequest(
            vrn = Vrn("123456789"),
            registeredCompanyName = "foo",
            tradingNames = Seq("Foo"),
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
              RegistrationWithoutFixedEstablishment(Country("FR", "France"), EuTaxIdentifier(Vat, "FR123")),
              RegistrationWithFixedEstablishment(
                Country("DE", "Germany"),
                EuTaxIdentifier(Vat, "DE123"),
                FixedEstablishment("Name", InternationalAddress("Line 1", None, "Town", None, None, Country("FR", "France")))
              )
            ),
            contactDetails = new ContactDetails(
              "Joe Bloggs",
              "01112223344",
              "email@email.com"
            ),
            websites = List("website1", "website2"),
            commencementDate = LocalDate.now,
            previousRegistrations = Seq(
              PreviousRegistration(Country("DE", "Germany"), "DE123")
            ),
            bankDetails = BankDetails("Account name", Some(bic), iban),
            isOnlineMarketplace = false,
            niPresence = Some(PrincipalPlaceOfBusinessInNi),
            dateOfFirstSale = Some(LocalDate.now)
          )

        EtmpRegistrationRequest.fromRegistrationRequest(registrationRequest) mustBe etmpRegistrationRequest
      }
    }
  }

}

