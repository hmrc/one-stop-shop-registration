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

class BankDetailsSpec extends AnyFreeSpec with Matchers {

  private val accountName = "account name"
  private val bic = Bic("ABCDEF2A").get
  private val iban = Iban("GB33BUKB20201555555555").toOption.get
  private val encryptedBic = "ABCDEF2A"
  private val encryptedIban = "GB33BUKB20201555555555"

  "BankDetails" - {


    "must deserialise/serialise to and from BankDetails" - {

      "when all optional values are present" in {

        val json = Json.obj(
          "accountName" -> accountName,
          "bic" -> bic,
          "iban" -> iban
        )

        val expectedResult = BankDetails(
          accountName = accountName,
          bic = Some(bic),
          iban = iban
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[BankDetails] mustBe JsSuccess(expectedResult)
      }

      "when all optional values are absent" in {

        val json = Json.obj(
          "accountName" -> accountName,
          "iban" -> iban
        )

        val expectedResult = BankDetails(
          accountName = accountName,
          bic = None,
          iban = iban
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[BankDetails] mustBe JsSuccess(expectedResult)
      }
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[BankDetails] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "accountName" -> 12345,
        "bic" -> bic,
        "iban" -> iban
      )

      json.validate[BankDetails] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "accountName" -> accountName,
        "bic" -> bic,
        "iban" -> JsNull
      )

      json.validate[BankDetails] mustBe a[JsError]
    }
  }

  "EncryptedBankDetails" - {


    "must deserialise/serialise to and from EncryptedBankDetails" - {

      "when all optional values are present" in {

        val json = Json.obj(
          "accountName" -> accountName,
          "bic" -> encryptedBic,
          "iban" -> encryptedIban
        )

        val expectedResult = EncryptedBankDetails(
          accountName = accountName,
          bic = Some(encryptedBic),
          iban = encryptedIban
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[EncryptedBankDetails] mustBe JsSuccess(expectedResult)
      }

      "when all optional values are absent" in {

        val json = Json.obj(
          "accountName" -> accountName,
          "iban" -> encryptedIban
        )

        val expectedResult = EncryptedBankDetails(
          accountName = accountName,
          bic = None,
          iban = encryptedIban
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[EncryptedBankDetails] mustBe JsSuccess(expectedResult)
      }
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[EncryptedBankDetails] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "accountName" -> 12345,
        "bic" -> encryptedBic,
        "iban" -> encryptedIban
      )

      json.validate[EncryptedBankDetails] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "accountName" -> accountName,
        "bic" -> encryptedBic,
        "iban" -> JsNull
      )

      json.validate[EncryptedBankDetails] mustBe a[JsError]
    }
  }
}
