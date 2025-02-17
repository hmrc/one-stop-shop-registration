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

import org.scalacheck.Arbitrary.arbitrary
import generators.Generators
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

class AddressSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators with OptionValues {

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

      "with all fields missing" in {

        val expectedJson = Json.obj(
        )

        expectedJson.validate[Address] mustBe a[JsError]
      }

      "with invalid fields" in {

        val expectedJson = Json.obj(
          "line1"       -> "line 1",
          "line2"       -> "line 2",
          "townOrCity"  -> "town",
          "county"      -> "county",
          "postCode"    -> "AA11 1AA",
          "country"     -> Json.obj(
            "code" -> 12345,
            "name" -> "United Kingdom"
          )
        )

        expectedJson.validate[Address] mustBe a[JsError]
      }

      "with null fields" in {

        val expectedJson = Json.obj(
          "line1"       -> "line 1",
          "line2"       -> "line 2",
          "townOrCity"  -> "town",
          "county"      -> "county",
          "postCode"    -> "AA11 1AA",
          "country"     -> Json.obj(
            "code" -> JsNull,
            "name" -> "United Kingdom"
          )
        )

        expectedJson.validate[Address] mustBe a[JsError]
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

      "with all fields missing" in {

        val expectedJson = Json.obj(
        )

        expectedJson.validate[InternationalAddress] mustBe a[JsError]
      }

      "with invalid fields" in {

        val expectedJson = Json.obj(
          "line1"         -> "line 1",
          "line2"         -> "line 2",
          "townOrCity"    -> "town",
          "stateOrRegion" -> "region",
          "postCode"      -> "AA11 1AA",
          "country"       -> Json.obj(
            "code" -> 12345,
            "name" -> "France"
          )
        )

        expectedJson.validate[InternationalAddress] mustBe a[JsError]
      }

      "with null fields" in {

        val expectedJson = Json.obj(
          "line1" -> "line 1",
          "line2" -> "line 2",
          "townOrCity" -> "town",
          "stateOrRegion" -> "region",
          "postCode" -> "AA11 1AA",
          "country" -> Json.obj(
            "code" -> JsNull,
            "name" -> "France"
          )
        )

        expectedJson.validate[InternationalAddress] mustBe a[JsError]
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

      "with all fields missing" in {

        val expectedJson = Json.obj(
        )

        expectedJson.validate[DesAddress] mustBe a[JsError]
      }

      "with invalid fields" in {

        val expectedJson = Json.obj(
          "line1"       -> "line 1",
          "line2"       -> "line 2",
          "line3"       -> "line 3",
          "line4"       -> "line 4",
          "line5"       -> "line 5",
          "postCode"    -> "postcode",
          "countryCode" -> 12345
        )

        expectedJson.validate[DesAddress] mustBe a[JsError]
      }

      "with null fields" in {

        val expectedJson = Json.obj(
          "line1"       -> "line 1",
          "line2"       -> "line 2",
          "line3"       -> "line 3",
          "line4"       -> "line 4",
          "line5"       -> "line 5",
          "postCode"    -> "postcode",
          "countryCode" -> JsNull
        )

        expectedJson.validate[DesAddress] mustBe a[JsError]
      }
    }
  }

  "EncryptedAddress" - {

    "must serialise and deserialise, with GB as the country, to and from a UK address" - {

      "with all optional fields present" in {

        forAll(arbitrary[String], arbitrary[String], arbitrary[String], arbitrary[String], arbitrary[String]) {
          case (line1, line2, town, county, postCode) =>

            val address: EncryptedAddress = EncryptedUkAddress(line1, Some(line2), town, Some(county), postCode)

            val expectedJson = Json.obj(
              "line1"       -> Json.toJson(line1),
              "line2"       -> Json.toJson(line2),
              "townOrCity"  -> Json.toJson(town),
              "county"      -> Json.toJson(county),
              "postCode"    -> Json.toJson(postCode),
              "country"     -> Json.obj(
                "code" -> "GB",
                "name" -> "United Kingdom"
              )
            )

            Json.toJson(address) mustEqual expectedJson
            expectedJson.validate[EncryptedAddress] mustEqual JsSuccess(address)
        }
      }

      "with all optional fields missing" in {

        forAll(arbitrary[String], arbitrary[String], arbitrary[String]) {
          case (line1, town, postCode) =>

            val address: EncryptedAddress = EncryptedUkAddress(line1, None, town, None, postCode)

            val expectedJson = Json.obj(
              "line1"       -> Json.toJson(line1),
              "townOrCity"  -> Json.toJson(town),
              "postCode"    -> Json.toJson(postCode),
              "country"     -> Json.obj(
                "code" -> "GB",
                "name" -> "United Kingdom"
              )
            )

            Json.toJson(address) mustEqual expectedJson
            expectedJson.validate[EncryptedAddress] mustEqual JsSuccess(address)
        }
      }

      "with all fields missing" in {

        val expectedJson = Json.obj(
        )

        expectedJson.validate[EncryptedAddress] mustBe a[JsError]
      }

      "with invalid fields" in {

        val expectedJson = Json.obj(
          "line1" -> "line 1",
          "line2" -> "line 2",
          "townOrCity" -> "town",
          "county" -> "county",
          "postCode" -> "AA11 1AA",
          "country" -> Json.obj(
            "code" -> 12345,
            "name" -> "United Kingdom"
          )
        )

        expectedJson.validate[EncryptedAddress] mustBe a[JsError]
      }

      "with null fields" in {

        val expectedJson = Json.obj(
          "line1" -> "line 1",
          "line2" -> "line 2",
          "townOrCity" -> "town",
          "county" -> "county",
          "postCode" -> "AA11 1AA",
          "country" -> Json.obj(
            "code" -> JsNull,
            "name" -> "United Kingdom"
          )
        )

        expectedJson.validate[EncryptedAddress] mustBe a[JsError]
      }

    }

    "must serialise and deserialise from and to an International address" - {

      "with all optional fields present" in {

        val countryGenerator = for {
          line1 <- arbitrary[String]
          line2 <- arbitrary[String]
          town  <- arbitrary[String]
          state <- arbitrary[String]
          postCode <- arbitrary[String]
          countryCode <- arbitrary[String]
          countryName <- arbitrary[String]
        } yield (line1, line2, town, state, postCode, countryCode, countryName)

        forAll(countryGenerator) {
          case (line1, line2, town, state, postCode, countryCode, countryName) =>

            val address = EncryptedInternationalAddress(line1, Some(line2), town, Some(state), Some(postCode), Country(countryCode, countryName))

            val expectedJson = Json.obj(
              "line1"         -> line1,
              "line2"         -> line2,
              "townOrCity"    -> town,
              "stateOrRegion" -> state,
              "postCode"      -> postCode,
              "country"       -> Json.obj(
                "code" -> countryCode,
                "name" -> countryName
              )
            )

            Json.toJson(address) mustEqual expectedJson
            expectedJson.validate[EncryptedAddress] mustEqual JsSuccess(address)
        }
      }

      "with all optional fields missing" in {


        val countryGenerator = for {
          line1 <- arbitrary[String]
          town  <- arbitrary[String]
          countryCode <- arbitrary[String]
          countryName <- arbitrary[String]
        } yield (line1, town, countryCode, countryName)

        forAll(countryGenerator) {
          case (line1, town, countryCode, countryName) =>

            val address = EncryptedInternationalAddress(line1, None, town, None, None, Country(countryCode, countryName))

            val expectedJson = Json.obj(
              "line1"         -> line1,
              "townOrCity"    -> town,
              "country"       -> Json.obj(
                "code" -> countryCode,
                "name" -> countryName
              )
            )

            Json.toJson(address) mustEqual expectedJson
            expectedJson.validate[EncryptedAddress] mustEqual JsSuccess(address)
        }
      }

      "with all fields missing" in {

        val expectedJson = Json.obj(
        )

        expectedJson.validate[EncryptedInternationalAddress] mustBe a[JsError]
      }

      "with invalid fields" in {

        val expectedJson = Json.obj(
          "line1" -> "line 1",
          "line2" -> "line 2",
          "townOrCity" -> "town",
          "stateOrRegion" -> "region",
          "postCode" -> "AA11 1AA",
          "country" -> Json.obj(
            "code" -> 12345,
            "name" -> "France"
          )
        )

        expectedJson.validate[EncryptedInternationalAddress] mustBe a[JsError]
      }

      "with null fields" in {

        val expectedJson = Json.obj(
          "line1" -> "line 1",
          "line2" -> "line 2",
          "townOrCity" -> "town",
          "stateOrRegion" -> "region",
          "postCode" -> "AA11 1AA",
          "country" -> Json.obj(
            "code" -> JsNull,
            "name" -> "France"
          )
        )

        expectedJson.validate[EncryptedInternationalAddress] mustBe a[JsError]
      }
    }

    "must serialise / deserialise from and to a DES address" - {

      "with all optional fields present" in {

        val countryGenerator = for {
          line1 <- arbitrary[String]
          line2 <- arbitrary[String]
          line3 <- arbitrary[String]
          line4 <- arbitrary[String]
          line5 <- arbitrary[String]
          postCode <- arbitrary[String]
          countryCode <- arbitrary[String]
        } yield (line1, line2, line3, line4, line5, postCode, countryCode)

        forAll(countryGenerator) {
          case (line1, line2, line3, line4, line5, postCode, countryCode) =>

            val address = EncryptedDesAddress(line1, Some(line2), Some(line3), Some(line4), Some(line5), Some(postCode), countryCode)

            val expectedJson = Json.obj(
              "line1"       -> line1,
              "line2"       -> line2,
              "line3"       -> line3,
              "line4"       -> line4,
              "line5"       -> line5,
              "postCode"    -> postCode,
              "countryCode" -> countryCode
            )

            Json.toJson(address) mustEqual expectedJson
            expectedJson.validate[EncryptedAddress] mustEqual JsSuccess(address)
        }
      }

      "with all optional fields missing" in {

        val countryGenerator = for {
          line1 <- arbitrary[String]
          countryCode <- arbitrary[String]
        } yield (line1, countryCode)

        forAll(countryGenerator) {
          case (line1, countryCode) =>

            val address = EncryptedDesAddress(line1, None, None, None, None, None, countryCode)

            val expectedJson = Json.obj(
              "line1"       -> line1,
              "countryCode" -> countryCode
            )

            Json.toJson(address) mustEqual expectedJson
            expectedJson.validate[EncryptedAddress] mustEqual JsSuccess(address)
        }
      }

      "with all fields missing" in {

        val expectedJson = Json.obj(
        )

        expectedJson.validate[EncryptedDesAddress] mustBe a[JsError]
      }

      "with invalid fields" in {

        val expectedJson = Json.obj(
          "line1" -> "line 1",
          "line2" -> "line 2",
          "line3" -> "line 3",
          "line4" -> "line 4",
          "line5" -> "line 5",
          "postCode" -> "postcode",
          "countryCode" -> 12345
        )

        expectedJson.validate[EncryptedDesAddress] mustBe a[JsError]
      }

      "with null fields" in {

        val expectedJson = Json.obj(
          "line1" -> "line 1",
          "line2" -> "line 2",
          "line3" -> "line 3",
          "line4" -> "line 4",
          "line5" -> "line 5",
          "postCode" -> "postcode",
          "countryCode" -> JsNull
        )

        expectedJson.validate[EncryptedDesAddress] mustBe a[JsError]
      }
    }
  }
}
