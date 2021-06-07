/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.{JsSuccess, Json}

class AddressSpec extends AnyFreeSpec with Matchers {

  "Address" - {

    "must serialise and deserialise, with GB as the country, to and from a UK address" - {

      "with all optional fields present" in {

        val address: Address = UkAddress("line 1", Some("line 2"), "town", Some("county"), "AA11 1AA")

        val expectedJson = Json.obj(
          "line1"       -> "line 1",
          "line2"       -> "line 2",
          "townOrCity"  -> "town",
          "county"      -> "county",
          "postCode"    -> "AA11 1AA",
          "country"     -> Json.obj(
            "code" -> "GB",
            "name" -> "United Kingdom"
          )
        )

        Json.toJson(address) mustEqual expectedJson
        expectedJson.validate[Address] mustEqual JsSuccess(address)
      }

      "with all optional fields missing" in {

        val address: Address = UkAddress("line 1", None, "town", None, "AA11 1AA")

        val expectedJson = Json.obj(
          "line1"       -> "line 1",
          "townOrCity"  -> "town",
          "postCode"    -> "AA11 1AA",
          "country"     -> Json.obj(
            "code" -> "GB",
            "name" -> "United Kingdom"
          )
        )

        Json.toJson(address) mustEqual expectedJson
        expectedJson.validate[Address] mustEqual JsSuccess(address)
      }
    }

    "must serialise and deserialise from and to an International address" - {

      "with all optional fields present" in {

        val address: Address = InternationalAddress("line 1", Some("line 2"), "town", Some("region"), Some("AA11 1AA"), Country("FR", "France"))

        val expectedJson = Json.obj(
          "line1"         -> "line 1",
          "line2"         -> "line 2",
          "townOrCity"    -> "town",
          "stateOrRegion" -> "region",
          "postCode"      -> "AA11 1AA",
          "country"       -> Json.obj(
            "code" -> "FR",
            "name" -> "France"
          )
        )

        Json.toJson(address) mustEqual expectedJson
        expectedJson.validate[Address] mustEqual JsSuccess(address)
      }

      "with all optional fields missing" in {

        val address: Address = InternationalAddress("line 1", None, "town", None, None, Country("FR", "France"))

        val expectedJson = Json.obj(
          "line1"         -> "line 1",
          "townOrCity"    -> "town",
          "country"       -> Json.obj(
            "code" -> "FR",
            "name" -> "France"
          )
        )

        Json.toJson(address) mustEqual expectedJson
        expectedJson.validate[Address] mustEqual JsSuccess(address)
      }
    }

    "must serialise / deserialise from and to a DES address" - {

      "with all optional fields present" in {

        val address: Address = DesAddress("line 1", Some("line 2"), Some("line 3"), Some("line 4"), Some("line 5"), Some("postcode"), "CC")

        val expectedJson = Json.obj(
          "line1"       -> "line 1",
          "line2"       -> "line 2",
          "line3"       -> "line 3",
          "line4"       -> "line 4",
          "line5"       -> "line 5",
          "postCode"    -> "postcode",
          "countryCode" -> "CC"
        )

        Json.toJson(address) mustEqual expectedJson
        expectedJson.validate[Address] mustEqual JsSuccess(address)
      }

      "with all optional fields missing" in {

        val address: Address = DesAddress("line 1", None, None, None, None, None, "CC")

        val expectedJson = Json.obj(
          "line1"       -> "line 1",
          "countryCode" -> "CC"
        )

        Json.toJson(address) mustEqual expectedJson
        expectedJson.validate[Address] mustEqual JsSuccess(address)
      }
    }
  }
}
