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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

class ContactDetailsSpec extends AnyFreeSpec with Matchers {

  "ContactDetails" - {
    
    "must deserialise/serialise to and from ContactDetails" - {

      "when all values are present" in {

        val json = Json.obj(
          "fullName" -> "Mr Test",
          "telephoneNumber" -> "1234567890",
          "emailAddress" -> "test@testEmail.com"
        )

        val expectedResult = ContactDetails(
          fullName = "Mr Test",
          telephoneNumber = "1234567890",
          emailAddress = "test@testEmail.com"
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[ContactDetails] mustBe JsSuccess(expectedResult)
      }
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[ContactDetails] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "fullName" -> 12345,
        "telephoneNumber" -> "1234567890",
        "emailAddress" -> "test@testEmail.com"
      )

      json.validate[ContactDetails] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "fullName" -> "Mr Test",
        "telephoneNumber" -> JsNull,
        "emailAddress" -> "test@testEmail.com"
      )

      json.validate[ContactDetails] mustBe a[JsError]
    }
  }

  "EncryptedContactDetails" - {


    "must deserialise/serialise to and from EncryptedContactDetails" - {

      "when all values are present" in {

        val json = Json.obj(
          "fullName" -> "Mr Test",
          "telephoneNumber" -> "1234567890",
          "emailAddress" -> "test@testEmail.com"
        )

        val expectedResult = EncryptedContactDetails(
          fullName = "Mr Test",
          telephoneNumber = "1234567890",
          emailAddress = "test@testEmail.com"
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[EncryptedContactDetails] mustBe JsSuccess(expectedResult)
      }
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[EncryptedContactDetails] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "fullName" -> "Mr Test",
        "telephoneNumber" -> "1234567890",
        "emailAddress" -> 12345
      )

      json.validate[EncryptedContactDetails] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "fullName" -> "Mr Test",
        "telephoneNumber" -> "1234567890",
        "emailAddress" -> JsNull
      )

      json.validate[EncryptedContactDetails] mustBe a[JsError]
    }
  }
}
