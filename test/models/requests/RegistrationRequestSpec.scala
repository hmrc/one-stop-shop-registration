/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.requests

import base.BaseSpec
import models.VatDetailSource.Etmp
import models.{BankDetails, ContactDetails, UkAddress, VatDetails}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

import java.time.{Instant, LocalDate}

class RegistrationRequestSpec extends BaseSpec with Matchers {

  "RegistrationRequest" - {


    "must deserialise/serialise to and from RegistrationRequest" - {

      "when all optional values are present" in {

        val json = Json.obj(
          "euRegistrations" -> Json.arr(),
          "commencementDate" -> LocalDate.now(),
          "nonCompliantReturns" -> "1",
          "websites" -> Json.arr(
            "www.example.com"
          ),
          "nonCompliantPayments" -> "2",
          "dateOfFirstSale" -> LocalDate.now(),
          "previousRegistrations" -> Json.arr(),
          "isOnlineMarketplace" -> false,
          "contactDetails" -> Json.obj(
            "fullName" -> "Test Contact",
            "telephoneNumber" -> "123456789",
            "emailAddress" -> "test@example.com"
          ),
          "bankDetails" -> Json.obj(
            "accountName" -> "BankAccount",
            "iban" -> "GB33BUKB20201555555555",
            "bic" -> "ABCDGB2A"
          ),
          "vrn" -> "123456789",
          "submissionReceived" -> Instant.now(stubClock),
          "vatDetails" -> Json.obj(
            "registrationDate" -> LocalDate.now(),
            "address" -> Json.obj(
              "line1" -> "Line 1",
              "townOrCity" -> "City",
              "postCode" -> "12345",
              "country" -> Json.obj(
                "code" -> "GB",
                "name" -> "United Kingdom"
              )
            ),
            "partOfVatGroup" -> false,
            "source" -> "etmp"
          ),
          "registeredCompanyName" -> "foo",
          "tradingNames" -> Json.arr(
            "Single"
          )
        )

        val expectedResult = RegistrationRequest(
          vrn = vrn,
          registeredCompanyName = "foo",
          tradingNames = List("Single"),
          vatDetails = VatDetails(
            registrationDate = LocalDate.now(),
            address = UkAddress("Line 1", None, "City", None, "12345"),
            partOfVatGroup = false,
            source = Etmp
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
          nonCompliantReturns = Some("1"),
          nonCompliantPayments = Some("2"),
          submissionReceived = Some(Instant.now(stubClock))
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[RegistrationRequest] mustBe JsSuccess(expectedResult)
      }
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[RegistrationRequest] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "euRegistrations" -> Json.arr(),
        "commencementDate" -> "2025-02-13",
        "nonCompliantReturns" -> 12345,
        "websites" -> Json.arr(
          "www.example.com"
        ),
        "nonCompliantPayments" -> "2",
        "dateOfFirstSale" -> "2025-02-13",
        "previousRegistrations" -> Json.arr(),
        "isOnlineMarketplace" -> false,
        "contactDetails" -> Json.obj(
          "fullName" -> "Test Contact",
          "telephoneNumber" -> "123456789",
          "emailAddress" -> "test@example.com"
        ),
        "bankDetails" -> Json.obj(
          "accountName" -> "BankAccount",
          "iban" -> "GB33BUKB20201555555555",
          "bic" -> "ABCDGB2A"
        ),
        "vrn" -> "123456789",
        "submissionReceived" -> "2025-02-13T00:00:00Z",
        "vatDetails" -> Json.obj(
          "registrationDate" -> "2025-02-13",
          "address" -> Json.obj(
            "line1" -> "Line 1",
            "townOrCity" -> "City",
            "postCode" -> "12345",
            "country" -> Json.obj(
              "code" -> "GB",
              "name" -> "United Kingdom"
            )
          ),
          "partOfVatGroup" -> false,
          "source" -> "etmp"
        ),
        "registeredCompanyName" -> "foo",
        "tradingNames" -> Json.arr(
          "Single"
        )
      )

      json.validate[RegistrationRequest] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "euRegistrations" -> Json.arr(),
        "commencementDate" -> "2025-02-13",
        "nonCompliantReturns" -> "1",
        "websites" -> Json.arr(
          "www.example.com"
        ),
        "nonCompliantPayments" -> "2",
        "dateOfFirstSale" -> "2025-02-13",
        "previousRegistrations" -> Json.arr(),
        "isOnlineMarketplace" -> JsNull,
        "contactDetails" -> Json.obj(
          "fullName" -> "Test Contact",
          "telephoneNumber" -> "123456789",
          "emailAddress" -> "test@example.com"
        ),
        "bankDetails" -> Json.obj(
          "accountName" -> "BankAccount",
          "iban" -> "GB33BUKB20201555555555",
          "bic" -> "ABCDGB2A"
        ),
        "vrn" -> "123456789",
        "submissionReceived" -> "2025-02-13T00:00:00Z",
        "vatDetails" -> Json.obj(
          "registrationDate" -> "2025-02-13",
          "address" -> Json.obj(
            "line1" -> "Line 1",
            "townOrCity" -> "City",
            "postCode" -> "12345",
            "country" -> Json.obj(
              "code" -> "GB",
              "name" -> "United Kingdom"
            )
          ),
          "partOfVatGroup" -> false,
          "source" -> "etmp"
        ),
        "registeredCompanyName" -> "foo",
        "tradingNames" -> Json.arr(
          "Single"
        )
      )

      json.validate[RegistrationRequest] mustBe a[JsError]
    }
  }
}
