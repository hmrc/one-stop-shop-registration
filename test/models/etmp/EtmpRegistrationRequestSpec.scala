package models.etmp

import base.BaseSpec
import models.EuTaxIdentifierType.{Other, Vat}
import models.VatDetailSource.UserEntered
import models._
import models.amend.{EtmpAmendRegistrationRequest, EtmpSelfExclusionReason}
import models.exclusions.ExclusionDetails
import models.requests.{AmendRegistrationRequest, RegistrationRequest}
import testutils.RegistrationData.registration
import uk.gov.hmrc.domain.Vrn

import java.time.{Instant, LocalDate}

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
            bankDetails = BankDetails("Account name", Some(bic), iban),
            isOnlineMarketplace = false,
            niPresence = Some(PrincipalPlaceOfBusinessInNi),
            dateOfFirstSale = Some(LocalDate.now),
            nonCompliantReturns = Some("1"),
            nonCompliantPayments = Some("2"),
            submissionReceived = None
          )

        EtmpRegistrationRequest.fromRegistrationRequest(registrationRequest, EtmpMessageType.OSSSubscriptionCreate) mustBe etmpRegistrationRequest
      }

      "should return a correctly mapped Etmp amend Registration Request when invoked" in {

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

        EtmpAmendRegistrationRequest.fromRegistrationRequest(registration, amendRegistrationRequest, EtmpMessageType.OSSSubscriptionAmend) mustBe etmpAmendRegistrationRequest
      }
    }
  }
}



