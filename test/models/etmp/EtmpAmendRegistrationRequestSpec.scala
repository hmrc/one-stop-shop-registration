package models.etmp

import base.BaseSpec
import models.*
import models.EuTaxIdentifierType.{Other, Vat}
import models.VatDetailSource.UserEntered
import models.amend.EtmpSelfExclusionReason
import models.exclusions.ExclusionDetails
import models.requests.{AmendRegistrationRequest, RegistrationRequest}
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}
import uk.gov.hmrc.domain.Vrn

import java.time.{Instant, LocalDate}

class EtmpAmendRegistrationRequestSpec extends BaseSpec {

  private val registrationRequest: RegistrationRequest = RegistrationRequest(
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
      RegistrationWithoutTaxId(
        Country("FR", "France")
      ),
      RegistrationWithFixedEstablishment(
        Country("DE", "Germany"),
        EuTaxIdentifier(Vat, "DE123"),
        TradeDetails("Name", InternationalAddress("Line 1", None, "Town", None, None, Country("DE", "Germany")))
      ),
      RegistrationWithoutFixedEstablishmentWithTradeDetails(
        Country("BE", "Belgium"),
        EuTaxIdentifier(Other, "12345"),
        TradeDetails("Name", InternationalAddress("Line 1", Some("Line 2"), "Town", None, None, Country("BE", "Belgium")))
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
      PreviousRegistrationNew(
        country = Country("DE", "Germany"),
        previousSchemesDetails = Seq(
          PreviousSchemeDetails(
            previousScheme = PreviousScheme.OSSU,
            previousSchemeNumbers = PreviousSchemeNumbers(
              previousSchemeNumber = "DE123",
              previousIntermediaryNumber = None
            )
          )
        )
      ),
      PreviousRegistrationLegacy(
        country = Country("BE", "Belgium"),
        vatNumber = "BE123"
      ),
      PreviousRegistrationNew(
        country = Country("EE", "Estonia"),
        previousSchemesDetails = Seq(
          PreviousSchemeDetails(
            previousScheme = PreviousScheme.OSSNU,
            previousSchemeNumbers = PreviousSchemeNumbers(
              previousSchemeNumber = "EE123",
              previousIntermediaryNumber = None
            )
          ),
          PreviousSchemeDetails(
            previousScheme = PreviousScheme.IOSSWI,
            previousSchemeNumbers = PreviousSchemeNumbers(
              previousSchemeNumber = "EE234",
              previousIntermediaryNumber = Some("IN234")
            )
          ),
          PreviousSchemeDetails(
            previousScheme = PreviousScheme.IOSSWOI,
            previousSchemeNumbers = PreviousSchemeNumbers(
              previousSchemeNumber = "EE312",
              previousIntermediaryNumber = None
            )
          )
        )
      )
    ),
    bankDetails = BankDetails("Account Foo", Some(bic), iban),
    isOnlineMarketplace = false,
    niPresence = Some(PrincipalPlaceOfBusinessInNi),
    dateOfFirstSale = Some(LocalDate.now),
    nonCompliantReturns = Some("1"),
    nonCompliantPayments = Some("2"),
    submissionReceived = Some(Instant.now)
  )

  "EtmpAmendRegistrationRequest" - {

    "should return a correctly mapped Etmp amend Registration Request when invoked" in {

      val amendRegistrationRequest: AmendRegistrationRequest = AmendRegistrationRequest(
        vrn = registrationRequest.vrn,
        registeredCompanyName = registrationRequest.registeredCompanyName,
        tradingNames = registrationRequest.tradingNames,
        vatDetails = registrationRequest.vatDetails,
        euRegistrations = registrationRequest.euRegistrations,
        contactDetails = registrationRequest.contactDetails,
        websites = registrationRequest.websites,
        commencementDate = registrationRequest.commencementDate,
        previousRegistrations = registrationRequest.previousRegistrations,
        bankDetails = registrationRequest.bankDetails,
        isOnlineMarketplace = registrationRequest.isOnlineMarketplace,
        niPresence = registrationRequest.niPresence,
        dateOfFirstSale = registrationRequest.dateOfFirstSale,
        nonCompliantReturns = registrationRequest.nonCompliantReturns,
        nonCompliantPayments = registrationRequest.nonCompliantPayments,
        submissionReceived = registrationRequest.submissionReceived,
        exclusionDetails = Some(ExclusionDetails(
          exclusionRequestDate = LocalDate.now,
          exclusionReason = EtmpSelfExclusionReason.NoLongerSupplies,
          movePOBDate = None,
          issuedBy = None,
          vatNumber = None
        )),
        rejoin = Some(false)
      )

      val json = Json.obj(
        "euRegistrations" -> Json.arr(
          Json.obj(
            "country" -> Json.obj(
              "code" -> "FR",
              "name" -> "France"
            )
          ),
          Json.obj(
            "country" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"
            ),
            "taxIdentifier" -> Json.obj(
              "identifierType" -> "vat",
              "value" -> "DE123"
            ),
            "fixedEstablishment" -> Json.obj(
              "tradingName" -> "Name",
              "address" -> Json.obj(
                "line1" -> "Line 1",
                "townOrCity" -> "Town",
                "country" -> Json.obj(
                  "code" -> "DE",
                  "name" -> "Germany"
                )
              )
            )
          ),
          Json.obj(
            "country" -> Json.obj(
              "code" -> "BE",
              "name" -> "Belgium"
            ),
            "taxIdentifier" -> Json.obj(
              "identifierType" -> "other",
              "value" -> "12345"
            ),
            "tradeDetails" -> Json.obj(
              "tradingName" -> "Name",
              "address" -> Json.obj(
                "line1" -> "Line 1",
                "townOrCity" -> "Town",
                "country" -> Json.obj(
                  "code" -> "BE",
                  "name" -> "Belgium"
                ),
                "line2" -> "Line 2"
              )
            )
          )
        ),
        "commencementDate" -> registrationRequest.commencementDate,
        "nonCompliantReturns" -> "1",
        "niPresence" -> "principalPlaceOfBusinessInNi",
        "nonCompliantPayments" -> "2",
        "dateOfFirstSale" -> registrationRequest.dateOfFirstSale,
        "previousRegistrations" -> Json.arr(
          Json.obj(
            "country" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"
            ),
            "previousSchemesDetails" -> Json.arr(
              Json.obj(
                "previousScheme" -> "ossu",
                "previousSchemeNumbers" -> Json.obj(
                  "previousSchemeNumber" -> "DE123"
                )
              )
            )
          ),
          Json.obj(
            "country" -> Json.obj(
              "code" -> "BE",
              "name" -> "Belgium"
            ),
            "vatNumber" -> "BE123"
          ),
          Json.obj(
            "country" -> Json.obj(
              "code" -> "EE",
              "name" -> "Estonia"
            ),
            "previousSchemesDetails" -> Json.arr(
              Json.obj(
                "previousScheme" -> "ossnu",
                "previousSchemeNumbers" -> Json.obj(
                  "previousSchemeNumber" -> "EE123"
                )
              ),
              Json.obj(
                "previousScheme" ->"iosswi",
                "previousSchemeNumbers" -> Json.obj(
                  "previousSchemeNumber" -> "EE234",
                  "previousIntermediaryNumber" -> "IN234"
                )
              ),
              Json.obj(
                "previousScheme" -> "iosswoi",
                "previousSchemeNumbers" -> Json.obj(
                  "previousSchemeNumber" -> "EE312"
                )
              )
            )
          )
        ),
        "isOnlineMarketplace" -> false,
        "contactDetails" -> Json.obj(
          "fullName" -> "Joe Bloggs",
          "telephoneNumber" -> "01112223344",
          "emailAddress" -> "email@email.com"
        ),
        "bankDetails" -> Json.obj(
          "accountName" -> "Account Foo",
          "iban" -> "GB33BUKB20201555555555",
          "bic" -> "ABCDGB2A"
        ),
        "vrn" -> "123456789",
        "submissionReceived" -> registrationRequest.submissionReceived,
        "registeredCompanyName" ->"foo",
        "tradingNames" -> Json.arr("Foo"),
        "rejoin" -> false,
        "websites" -> Json.arr("website1","website2"),
        "vatDetails" -> Json.obj(
          "registrationDate" -> LocalDate.now,
          "address" -> Json.obj(
            "postCode" ->"AA12 1AB",
            "line4" -> "county",
            "line1" -> "123 Street",
            "countryCode" -> "GB",
            "line2" -> "Street",
            "line3" -> "City"
          ),
          "partOfVatGroup" -> true,
          "source" -> "userEntered"
        ),
        "exclusionDetails" -> Json.obj(
          "exclusionRequestDate" -> LocalDate.now,
          "exclusionReason" ->"1"
        )
      )

      Json.toJson(amendRegistrationRequest) mustBe json
      json.validate[AmendRegistrationRequest] mustBe JsSuccess(amendRegistrationRequest)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[AmendRegistrationRequest] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "euRegistrations" -> Json.arr(
          Json.obj(
            "country" -> Json.obj(
              "code" -> "FR",
              "name" -> "France"
            )
          ),
          Json.obj(
            "country" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"
            ),
            "taxIdentifier" -> Json.obj(
              "identifierType" -> "vat",
              "value" -> "DE123"
            ),
            "fixedEstablishment" -> Json.obj(
              "tradingName" -> "Name",
              "address" -> Json.obj(
                "line1" -> "Line 1",
                "townOrCity" -> "Town",
                "country" -> Json.obj(
                  "code" -> "DE",
                  "name" -> "Germany"
                )
              )
            )
          ),
          Json.obj(
            "country" -> Json.obj(
              "code" -> 12345, //Invalid Json
              "name" -> "Belgium"
            ),
            "taxIdentifier" -> Json.obj(
              "identifierType" -> "other",
              "value" -> "12345"
            ),
            "tradeDetails" -> Json.obj(
              "tradingName" -> "Name",
              "address" -> Json.obj(
                "line1" -> "Line 1",
                "townOrCity" -> "Town",
                "country" -> Json.obj(
                  "code" -> "BE",
                  "name" -> "Belgium"
                ),
                "line2" -> "Line 2"
              )
            )
          )
        ),
        "commencementDate" -> registrationRequest.commencementDate,
        "nonCompliantReturns" -> "1",
        "niPresence" -> "principalPlaceOfBusinessInNi",
        "nonCompliantPayments" -> "2",
        "dateOfFirstSale" -> registrationRequest.dateOfFirstSale,
        "previousRegistrations" -> Json.arr(
          Json.obj(
            "country" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"
            ),
            "previousSchemesDetails" -> Json.arr(
              Json.obj(
                "previousScheme" -> "ossu",
                "previousSchemeNumbers" -> Json.obj(
                  "previousSchemeNumber" -> "DE123"
                )
              )
            )
          ),
          Json.obj(
            "country" -> Json.obj(
              "code" -> "BE",
              "name" -> "Belgium"
            ),
            "vatNumber" -> "BE123"
          ),
          Json.obj(
            "country" -> Json.obj(
              "code" -> "EE",
              "name" -> "Estonia"
            ),
            "previousSchemesDetails" -> Json.arr(
              Json.obj(
                "previousScheme" -> "ossnu",
                "previousSchemeNumbers" -> Json.obj(
                  "previousSchemeNumber" -> "EE123"
                )
              ),
              Json.obj(
                "previousScheme" ->"iosswi",
                "previousSchemeNumbers" -> Json.obj(
                  "previousSchemeNumber" -> "EE234",
                  "previousIntermediaryNumber" -> "IN234"
                )
              ),
              Json.obj(
                "previousScheme" -> "iosswoi",
                "previousSchemeNumbers" -> Json.obj(
                  "previousSchemeNumber" -> "EE312"
                )
              )
            )
          )
        ),
        "isOnlineMarketplace" -> false,
        "contactDetails" -> Json.obj(
          "fullName" -> "Joe Bloggs",
          "telephoneNumber" -> "01112223344",
          "emailAddress" -> "email@email.com"
        ),
        "bankDetails" -> Json.obj(
          "accountName" -> "Account Foo",
          "iban" -> "GB33BUKB20201555555555",
          "bic" -> "ABCDGB2A"
        ),
        "vrn" -> "123456789",
        "submissionReceived" -> registrationRequest.submissionReceived,
        "registeredCompanyName" ->"foo",
        "tradingNames" -> Json.arr("Foo"),
        "rejoin" -> false,
        "websites" -> Json.arr("website1","website2"),
        "vatDetails" -> Json.obj(
          "registrationDate" -> LocalDate.now,
          "address" -> Json.obj(
            "postCode" ->"AA12 1AB",
            "line4" -> "county",
            "line1" -> "123 Street",
            "countryCode" -> "GB",
            "line2" -> "Street",
            "line3" -> "City"
          ),
          "partOfVatGroup" -> true,
          "source" -> "userEntered"
        ),
        "exclusionDetails" -> Json.obj(
          "exclusionRequestDate" -> LocalDate.now,
          "exclusionReason" ->"1"
        )
      )

      json.validate[AmendRegistrationRequest] mustBe a[JsError]
    }

    "must handle null data during deserialization" in {

      val json = Json.obj(
        "euRegistrations" -> Json.arr(
          Json.obj(
            "country" -> Json.obj(
              "code" -> "FR",
              "name" -> "France"
            )
          ),
          Json.obj(
            "country" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"
            ),
            "taxIdentifier" -> Json.obj(
              "identifierType" -> "vat",
              "value" -> "DE123"
            ),
            "fixedEstablishment" -> Json.obj(
              "tradingName" -> "Name",
              "address" -> Json.obj(
                "line1" -> "Line 1",
                "townOrCity" -> "Town",
                "country" -> Json.obj(
                  "code" -> "DE",
                  "name" -> "Germany"
                )
              )
            )
          ),
          Json.obj(
            "country" -> Json.obj(
              "code" -> JsNull, //Null Json
              "name" -> "Belgium"
            ),
            "taxIdentifier" -> Json.obj(
              "identifierType" -> "other",
              "value" -> "12345"
            ),
            "tradeDetails" -> Json.obj(
              "tradingName" -> "Name",
              "address" -> Json.obj(
                "line1" -> "Line 1",
                "townOrCity" -> "Town",
                "country" -> Json.obj(
                  "code" -> "BE",
                  "name" -> "Belgium"
                ),
                "line2" -> "Line 2"
              )
            )
          )
        ),
        "commencementDate" -> registrationRequest.commencementDate,
        "nonCompliantReturns" -> "1",
        "niPresence" -> "principalPlaceOfBusinessInNi",
        "nonCompliantPayments" -> "2",
        "dateOfFirstSale" -> registrationRequest.dateOfFirstSale,
        "previousRegistrations" -> Json.arr(
          Json.obj(
            "country" -> Json.obj(
              "code" -> "DE",
              "name" -> "Germany"
            ),
            "previousSchemesDetails" -> Json.arr(
              Json.obj(
                "previousScheme" -> "ossu",
                "previousSchemeNumbers" -> Json.obj(
                  "previousSchemeNumber" -> "DE123"
                )
              )
            )
          ),
          Json.obj(
            "country" -> Json.obj(
              "code" -> "BE",
              "name" -> "Belgium"
            ),
            "vatNumber" -> "BE123"
          ),
          Json.obj(
            "country" -> Json.obj(
              "code" -> "EE",
              "name" -> "Estonia"
            ),
            "previousSchemesDetails" -> Json.arr(
              Json.obj(
                "previousScheme" -> "ossnu",
                "previousSchemeNumbers" -> Json.obj(
                  "previousSchemeNumber" -> "EE123"
                )
              ),
              Json.obj(
                "previousScheme" -> "iosswi",
                "previousSchemeNumbers" -> Json.obj(
                  "previousSchemeNumber" -> "EE234",
                  "previousIntermediaryNumber" -> "IN234"
                )
              ),
              Json.obj(
                "previousScheme" -> "iosswoi",
                "previousSchemeNumbers" -> Json.obj(
                  "previousSchemeNumber" -> "EE312"
                )
              )
            )
          )
        ),
        "isOnlineMarketplace" -> false,
        "contactDetails" -> Json.obj(
          "fullName" -> "Joe Bloggs",
          "telephoneNumber" -> "01112223344",
          "emailAddress" -> "email@email.com"
        ),
        "bankDetails" -> Json.obj(
          "accountName" -> "Account Foo",
          "iban" -> "GB33BUKB20201555555555",
          "bic" -> "ABCDGB2A"
        ),
        "vrn" -> "123456789",
        "submissionReceived" -> registrationRequest.submissionReceived,
        "registeredCompanyName" -> "foo",
        "tradingNames" -> Json.arr("Foo"),
        "rejoin" -> false,
        "websites" -> Json.arr("website1", "website2"),
        "vatDetails" -> Json.obj(
          "registrationDate" -> LocalDate.now,
          "address" -> Json.obj(
            "postCode" -> "AA12 1AB",
            "line4" -> "county",
            "line1" -> "123 Street",
            "countryCode" -> "GB",
            "line2" -> "Street",
            "line3" -> "City"
          ),
          "partOfVatGroup" -> true,
          "source" -> "userEntered"
        ),
        "exclusionDetails" -> Json.obj(
          "exclusionRequestDate" -> LocalDate.now,
          "exclusionReason" -> "1"
        )
      )

      json.validate[AmendRegistrationRequest] mustBe a[JsError]
    }
  }
}



