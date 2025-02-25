package models

import base.BaseSpec
import org.scalatest.matchers.should.Matchers.{should, shouldEqual}
import play.api.libs.json.{JsSuccess, JsValue, Json}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate

class EncryptedRegistrationSpec extends BaseSpec {

    "EncryptedRegistration" - {
        "should serialise and deserialise correctly" in {
            val encryptedRegistration = EncryptedRegistration(
                vrn = Vrn("123456789"),
                registeredCompanyName = "Foo",
                tradingNames = Seq("tradingName1", "tradingName2"),
                vatDetails = EncryptedVatDetails(
                    registrationDate = LocalDate.now(),
                    address = EncryptedUkAddress("line1", None, "City", county = None, postCode = "12345"),
                    partOfVatGroup = "no",
                    source = VatDetailSource.Etmp),
                euRegistrations = Seq.empty,
                contactDetails = EncryptedContactDetails("Joe Bloggs", "1234567890", "test@test.com"),
                websites = Seq("website1", "website2"),
                commencementDate = LocalDate.now(),
                previousRegistrations = Seq.empty,
                bankDetails = EncryptedBankDetails("accountName", Some("bic"), "iban"),
                isOnlineMarketplace = "false",
                niPresence = None,
                submissionReceived = None,
                lastUpdated = None,
                dateOfFirstSale = None,
                nonCompliantReturns = None,
                nonCompliantPayments = None)


            val json = Json.toJson(encryptedRegistration)
            
            val expectedJson = Json.obj(
                "vrn" -> "123456789",
                "registeredCompanyName" -> "Foo",
                "tradingNames" -> Seq("tradingName1", "tradingName2"),
                "vatDetails" -> Json.obj(
                    "registrationDate" -> LocalDate.now.toString,
                    "address" -> Json.obj(
                        "line1" -> "line1",
                        "townOrCity" -> "City",
                        "postCode" -> "12345",
                        "country" -> Json.obj(
                            "code" -> "GB",
                            "name" -> "United Kingdom"
                        )
                    ),
                    "partOfVatGroup" -> "no",
                    "source" -> "etmp"),
                "euRegistrations" -> Seq.empty[JsValue],
                "contactDetails" -> Json.obj(
                    "fullName" -> "Joe Bloggs",
                    "telephoneNumber" -> "1234567890",
                    "emailAddress" -> "test@test.com"
                ),
                "websites" -> Seq("website1", "website2"),
                "commencementDate" -> LocalDate.now().toString,
                "previousRegistrations" -> Seq.empty[JsValue],
                "bankDetails" -> Json.obj(
                    "accountName" -> "accountName",
                    "iban" -> "iban",
                    "bic" -> "bic"
                ),
                "isOnlineMarketplace" -> "false",
            )

            json shouldEqual expectedJson
            expectedJson.validate[EncryptedRegistration] mustEqual JsSuccess(encryptedRegistration)
        }
    }
}
