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

class TradeDetailsSpec extends BaseSpec with Matchers {

  "TradeDetails" - {

    "must deserialise/serialise to and from TradeDetails" in {

      val json = Json.obj(
        "tradingName" -> "Trading Name",
        "address" -> Json.obj(
          "postCode" -> "Postcode",
          "country" -> Json.obj(
            "code" -> "DE",
            "name" -> "Germany"
          ),
          "line1" -> "Line 1",
          "townOrCity" -> "Town",
          "line2" -> "Line 2",
          "stateOrRegion" -> "Region"
        )
      )

      val expectedResult = TradeDetails(
        tradingName = "Trading Name",
        address = InternationalAddress(
          line1 = "Line 1",
          line2 = Some("Line 2"),
          townOrCity = "Town",
          stateOrRegion = Some("Region"),
          postCode = Some("Postcode"),
          country = Country("DE", "Germany")
        )
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[TradeDetails] mustBe JsSuccess(expectedResult)
    }

    "when all optional values are absent" in {

      val json = Json.obj(
        "tradingName" -> "Trading Name",
        "address" -> Json.obj(
          "country" -> Json.obj(
            "code" -> "DE",
            "name" -> "Germany"
          ),
          "line1" -> "Line 1",
          "townOrCity" -> "Town"
        )
      )

      val expectedResult = TradeDetails(
        tradingName = "Trading Name",
        address = InternationalAddress(
          line1 = "Line 1",
          line2 = None,
          townOrCity = "Town",
          stateOrRegion = None,
          postCode = None,
          country = Country("DE", "Germany")
        )

      )

      Json.toJson(expectedResult) mustBe json
      json.validate[TradeDetails] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[TradeDetails] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "tradingName" -> 12345,
        "address" -> Json.obj(
          "postCode" -> "Postcode",
          "country" -> Json.obj(
            "code" -> "DE",
            "name" -> "Germany"
          ),
          "line1" -> "Line 1",
          "townOrCity" -> "Town",
          "line2" -> "Line 2",
          "stateOrRegion" -> "Region"
        )
      )

      json.validate[TradeDetails] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "tradingName" -> "Trading Name",
        "address" -> Json.obj(
          "postCode" -> "Postcode",
          "country" -> Json.obj(
            "code" -> "DE",
            "name" -> JsNull
          ),
          "line1" -> "Line 1",
          "townOrCity" -> "Town",
          "line2" -> "Line 2",
          "stateOrRegion" -> "Region"
        )
      )

      json.validate[TradeDetails] mustBe a[JsError]
    }
  }

  "EncryptedTradeDetails" - {
    
    "must deserialise/serialise to and from EncryptedTradeDetails" in {

      val json = Json.obj(
        "tradingName" -> "Trading Name",
        "address" -> Json.obj(
          "postCode" -> "Postcode",
          "country" -> Json.obj(
            "code" -> "DE",
            "name" -> "Germany"
          ),
          "line1" -> "Line 1",
          "townOrCity" -> "Town",
          "line2" -> "Line 2",
          "stateOrRegion" -> "Region"
        )
      )

      val expectedResult = EncryptedTradeDetails(
        tradingName = "Trading Name",
        address = EncryptedInternationalAddress(
          line1 = "Line 1",
          line2 = Some("Line 2"),
          townOrCity = "Town",
          stateOrRegion = Some("Region"),
          postCode = Some("Postcode"),
          country = Country("DE", "Germany")
        )
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EncryptedTradeDetails] mustBe JsSuccess(expectedResult)
    }

    "when all optional values are absent" in {

      val json = Json.obj(
        "tradingName" -> "Trading Name",
        "address" -> Json.obj(
          "country" -> Json.obj(
            "code" -> "DE",
            "name" -> "Germany"
          ),
          "line1" -> "Line 1",
          "townOrCity" -> "Town"
        )
      )

      val expectedResult = EncryptedTradeDetails(
        tradingName = "Trading Name",
        address = EncryptedInternationalAddress(
          line1 = "Line 1",
          line2 = None,
          townOrCity = "Town",
          stateOrRegion = None,
          postCode = None,
          country = Country("DE", "Germany")
        )

      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EncryptedTradeDetails] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()

      json.validate[EncryptedTradeDetails] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "tradingName" -> 12345,
        "address" -> Json.obj(
          "postCode" -> "Postcode",
          "country" -> Json.obj(
            "code" -> "DE",
            "name" -> "Germany"
          ),
          "line1" -> "Line 1",
          "townOrCity" -> "Town",
          "line2" -> "Line 2",
          "stateOrRegion" -> "Region"
        )
      )

      json.validate[EncryptedTradeDetails] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "tradingName" -> "Trading Name",
        "address" -> Json.obj(
          "postCode" -> "Postcode",
          "country" -> Json.obj(
            "code" -> "DE",
            "name" -> JsNull
          ),
          "line1" -> "Line 1",
          "townOrCity" -> "Town",
          "line2" -> "Line 2",
          "stateOrRegion" -> "Region"
        )
      )

      json.validate[EncryptedTradeDetails] mustBe a[JsError]
    }
  }
}
