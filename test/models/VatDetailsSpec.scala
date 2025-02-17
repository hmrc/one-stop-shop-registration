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

package models

import base.BaseSpec
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

import java.time.LocalDate

class VatDetailsSpec extends BaseSpec with Matchers {

  "VatDetails" - {

    "must deserialise/serialise to and from VatDetails" in {

      val json = Json.obj(
        "registrationDate" -> LocalDate.now(stubClock),
        "address" -> Json.obj(
          "line1" -> "Line 1",
          "countryCode" -> "GB",
          "postCode" -> "AA11 1AA"
        ),
        "partOfVatGroup" -> false,
        "source" -> "etmp"
      )

      val expectedResult = VatDetails(
        registrationDate = LocalDate.now(stubClock),
        address = DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
        partOfVatGroup = false,
        source = VatDetailSource.Etmp
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[VatDetails] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[VatDetails] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "registrationDate" -> LocalDate.now(stubClock),
        "address" -> Json.obj(
          "line1" -> "Line 1",
          "countryCode" -> "GB",
          "postCode" -> "AA11 1AA"
        ),
        "partOfVatGroup" -> false,
        "source" -> 12345
      )

      json.validate[VatDetails] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "registrationDate" -> LocalDate.now(stubClock),
        "address" -> Json.obj(
          "line1" -> "Line 1",
          "countryCode" -> "GB",
          "postCode" -> "AA11 1AA"
        ),
        "partOfVatGroup" -> false,
        "source" -> JsNull
      )

      json.validate[VatDetails] mustBe a[JsError]
    }
  }

  "EncryptedVatDetails" - {
    
    "must deserialise/serialise to and from EncryptedVatDetails" in {

      val json = Json.obj(
        "registrationDate" -> LocalDate.now(stubClock),
        "address" -> Json.obj(
          "line1" -> "Line 1",
          "countryCode" -> "GB",
          "postCode" -> "AA11 1AA"
        ),
        "partOfVatGroup" -> "false",
        "source" -> "etmp"
      )

      val expectedResult = EncryptedVatDetails(
        registrationDate = LocalDate.now(stubClock),
        address = EncryptedDesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
        partOfVatGroup = "false",
        source = VatDetailSource.Etmp
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EncryptedVatDetails] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[EncryptedVatDetails] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "registrationDate" -> LocalDate.now(stubClock),
        "address" -> Json.obj(
          "line1" -> "Line 1",
          "countryCode" -> "GB",
          "postCode" -> "AA11 1AA"
        ),
        "partOfVatGroup" -> "false",
        "source" -> 12345
      )

      json.validate[EncryptedVatDetails] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "registrationDate" -> LocalDate.now(stubClock),
        "address" -> Json.obj(
          "line1" -> "Line 1",
          "countryCode" -> "GB",
          "postCode" -> "AA11 1AA"
        ),
        "partOfVatGroup" -> "false",
        "source" -> JsNull
      )

      json.validate[EncryptedVatDetails] mustBe a[JsError]
    }
  }
}
