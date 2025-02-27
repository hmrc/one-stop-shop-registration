package models.etmp

import base.BaseSpec
import models.BankDetails
import models.exclusions.ExclusionReason
import play.api.libs.json.{JsSuccess, Json}

import java.time.{LocalDate, LocalDateTime}

class EtmpDisplayRegistrationSpec extends BaseSpec {

  "EtmpDisplayRegistration" - {

    "must deserialise" - {

      "when all optional fields are present" in {

        val json = Json.obj(
          "tradingNames" -> Json.arr(
            Json.obj(
              "tradingName" -> "French Trading Company"
            )
          ),
          "schemeDetails" -> Json.obj(
            "commencementDate" -> "2023-01-01",
            "firstSaleDate" -> "2023-01-25",
            "nonCompliantReturns" -> "1",
            "nonCompliantPayments" -> "2",
            "euRegistrationDetails" -> Json.arr(
              Json.obj(
                "issuedBy" -> "FR",
                "vatNumber" -> "FR123456789",
                "taxIdentificationNumber" -> "123456789",
                "fixedEstablishment" -> true,
                "fixedEstablishmentTradingName" -> "French Trading Company",
                "fixedEstablishmentAddressLine1" -> "Line1",
                "fixedEstablishmentAddressLine2" -> "Line2",
                "townOrCity" -> "Town",
                "regionOrState" -> "Region",
                "postcode" -> "Postcode"
              )
            ),
            "previousEURegistrationDetails" -> Json.arr(
              Json.obj(
                "issuedBy" -> "ES",
                "registrationNumber" -> "ES123456789",
                "schemeType" -> "IOSS with intermediary",
                "intermediaryNumber" -> "IN7241234567"
              )
            ),
            "onlineMarketPlace" -> true,
            "websites" -> Json.arr(
              Json.obj(
                "websiteAddress" -> "www.testWebsite.com"
              )
            ),
            "contactDetails" -> Json.obj(
              "contactNameOrBusinessAddress" -> "Mr Test",
              "businessTelephoneNumber" -> "1234567890",
              "businessEmailAddress" -> "test@testEmail.com"
            ),
            "exclusions" -> Json.arr(
              Json.obj(
                "exclusionReason" -> "4",
                "effectiveDate" -> "2024-02-25",
                "decisionDate" -> "2024-04-25",
                "quarantine" -> true
              )
            )
          ),
          "bankDetails" -> Json.obj(
            "accountName" -> "Bank Account Name",
            "bic" -> "ABCDGB2A",
            "iban" -> "GB33BUKB20201555555555"
          ),
          "adminUse" -> Json.obj(
            "changeDate" -> LocalDateTime.now(stubClock)
          )
        )

        val expectedResult = EtmpDisplayRegistration(
          tradingNames = Seq(
            EtmpTradingNames(
              tradingName = "French Trading Company"
            )
          ),
          schemeDetails = EtmpDisplaySchemeDetails(
            commencementDate = LocalDate.of(2023, 1, 1).format(dateFormatter),
            firstSaleDate = Some(LocalDate.of(2023, 1, 25).format(dateFormatter)),
            euRegistrationDetails = Seq(
              EtmpEuRegistrationDetails(
                countryOfRegistration = "FR",
                vatNumber = Some("FR123456789"),
                taxIdentificationNumber = Some("123456789"),
                fixedEstablishment = Some(true),
                tradingName = Some("French Trading Company"),
                fixedEstablishmentAddressLine1 = Some("Line1"),
                fixedEstablishmentAddressLine2 = Some("Line2"),
                townOrCity = Some("Town"),
                regionOrState = Some("Region"),
                postcode = Some("Postcode")
              )
            ),
            previousEURegistrationDetails = Seq(
              EtmpPreviousEURegistrationDetails(
                issuedBy = "ES",
                registrationNumber = "ES123456789",
                schemeType = SchemeType.IOSSWithIntermediary,
                intermediaryNumber = Some("IN7241234567")
              )
            ),
            onlineMarketPlace = true,
            websites = Seq(
              Website(
                websiteAddress = "www.testWebsite.com"
              )
            ),
            contactName = "Mr Test",
            businessTelephoneNumber = "1234567890",
            businessEmailId = "test@testEmail.com",
            nonCompliantReturns = Some("1"),
            nonCompliantPayments = Some("2"),
            exclusions = Seq(EtmpExclusion(
              exclusionReason = ExclusionReason.FailsToComply,
              effectiveDate = LocalDate.parse("2024-02-25"),
              decisionDate = LocalDate.parse("2024-04-25"),
              quarantine = true
            )),
            unusableStatus = None
          ),
          bankDetails = BankDetails(
            accountName = "Bank Account Name",
            Some(bic),
            iban
          ),
          adminUse = AdminUse(Some(LocalDateTime.now(stubClock)))
        )

        json.validate[EtmpDisplayRegistration] mustEqual JsSuccess(expectedResult)
      }

      "when all optional fields are absent" in {

        val json = Json.obj(
          "tradingNames" -> Json.arr(),
          "schemeDetails" -> Json.obj(
            "commencementDate" -> "2023-01-01",
            "euRegistrationDetails" -> Json.arr(),
            "previousEURegistrationDetails" -> Json.arr(),
            "onlineMarketPlace" -> true,
            "websites" -> Json.arr(),
            "contactDetails" -> Json.obj(
              "contactNameOrBusinessAddress" -> "Mr Test",
              "businessTelephoneNumber" -> "1234567890",
              "businessEmailAddress" -> "test@testEmail.com"
            )
          ),
          "bankDetails" -> Json.obj(
            "accountName" -> "Bank Account Name",
            "iban" -> "GB33BUKB20201555555555"
          ),
          "adminUse" -> Json.obj(

          )
        )

        val expectedResult = EtmpDisplayRegistration(
          tradingNames = Seq.empty,
          schemeDetails = EtmpDisplaySchemeDetails(
            commencementDate = LocalDate.of(2023, 1, 1).format(dateFormatter),
            None,
            None,
            None,
            euRegistrationDetails = Seq.empty,
            previousEURegistrationDetails = Seq.empty,
            onlineMarketPlace = true,
            websites = Seq.empty,
            contactName = "Mr Test",
            businessTelephoneNumber = "1234567890",
            businessEmailId = "test@testEmail.com",
            None,
            None,
            exclusions = Seq.empty,
            unusableStatus = None
          ),
          bankDetails = BankDetails(
            accountName = "Bank Account Name",
            None,
            iban
          ),
          adminUse = AdminUse(None)
        )

        json.validate[EtmpDisplayRegistration] mustEqual JsSuccess(expectedResult)
      }
    }
  }
}