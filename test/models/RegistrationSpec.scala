package models

import models.etmp.{AdminUse, EtmpDisplaySchemeDetails, Website}
import base.BaseSpec
import org.scalatest.matchers.should.Matchers.should
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate

class RegistrationSpec extends BaseSpec {

    "fromEtmpRegistration" - {
        "should return a Registration correctly from EtmpRegistration data" in {
            val vrn = Vrn("123456789")
            val vatDetails = vatCustomerInfo
            val adminUse = AdminUse(None)
            val schemeDetails = EtmpDisplaySchemeDetails(
                commencementDate = LocalDate.now().format(dateFormatter),
                firstSaleDate = Some(LocalDate.now().format(dateFormatter)),
                euRegistrationDetails = Seq.empty,
                previousEURegistrationDetails = Seq.empty,
                onlineMarketPlace = false,
                websites = Seq(Website("website1"), Website("website2")),
                contactName = "Joe Bloggs",
                businessTelephoneNumber = "01112223344",
                businessEmailId = "email@email.com",
                nonCompliantReturns = None,
                nonCompliantPayments = None,
                exclusions = Seq.empty,
            )

            val result = Registration.fromEtmpRegistration(
                vrn,
                vatDetails,
                etmpRegistrationRequest.tradingNames,
                schemeDetails,
                etmpRegistrationRequest.bankDetails,
                adminUse)

            result.vrn mustBe vrn
            result.registeredCompanyName mustBe "Company name"
            result.tradingNames mustBe Seq("Foo")
            result.vatDetails mustBe VatDetails(vatDetails.registrationDate.get, vatDetails.address, vatDetails.partOfVatGroup, VatDetailSource.Etmp)
            result.contactDetails.fullName mustBe "Joe Bloggs"
            result.websites should contain("website2")

        }


        "throw an exception if no organisation name or individual name is provided" in {
            val vrn = Vrn("123456789")
            val vatDetails = vatCustomerInfo.copy(individualName = None, organisationName = None)
            val adminUse = AdminUse(None)
            val schemeDetails = EtmpDisplaySchemeDetails(
                commencementDate = LocalDate.now().format(dateFormatter),
                firstSaleDate = Some(LocalDate.now().format(dateFormatter)),
                euRegistrationDetails = Seq.empty,
                previousEURegistrationDetails = Seq.empty,
                onlineMarketPlace = false,
                websites = Seq(Website("website1"), Website("website2")),
                contactName = "Joe Bloggs",
                businessTelephoneNumber = "01112223344",
                businessEmailId = "email@email.com",
                nonCompliantReturns = None,
                nonCompliantPayments = None,
                exclusions = Seq.empty,
            )

            an[IllegalStateException] mustBe thrownBy {
                Registration.fromEtmpRegistration(vrn, vatDetails, etmpRegistrationRequest.tradingNames, schemeDetails,  etmpRegistrationRequest.bankDetails, adminUse)
            }
        }

        "serialize and deserialize to/from JSON correctly" in {
            val registration = Registration(
                vrn = Vrn("123456789"),
                registeredCompanyName = "Test Company",
                tradingNames = Seq("Trading Name 1", "Trading Name 2"),
                vatDetails = VatDetails(
                    registrationDate = LocalDate.now(),
                    address = UkAddress("Line 1", None, "City", None, "12345"),
                    partOfVatGroup = false,
                    source = VatDetailSource.Etmp
                ),
                euRegistrations = Seq.empty,
                contactDetails = ContactDetails("Test Contact", "123456789", "test@example.com"),
                websites = Seq("www.example.com"),
                commencementDate = LocalDate.now(),
                previousRegistrations = Seq.empty,
                bankDetails = BankDetails("BankAccount", Some(bic), iban),
                isOnlineMarketplace = false,
                niPresence = None,
                dateOfFirstSale = Some(LocalDate.now()),
                submissionReceived = None,
                lastUpdated = None,
                adminUse = AdminUse(None)
            )

            val json = Json.toJson(registration)
            val result = json.as[Registration]

            result mustBe registration
        }
    }
}
