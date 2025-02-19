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

class PreviousRegistrationSpec extends BaseSpec with Matchers {

  "PreviousRegistrationNew" - {

    "must deserialise/serialise to and from PreviousRegistrationNew" - {

      "when all values are present" in {

        val json = Json.obj(
          "country" -> Json.obj(
            "code" -> "DE",
            "name" -> "Germany"
          ),
          "previousSchemesDetails" -> Json.arr(
            Json.obj(
              "previousScheme" -> "ossu",
              "previousSchemeNumbers" -> Json.obj(
                "previousSchemeNumber" -> "DE123",
                "previousIntermediaryNumber" -> "IN123"
              )
            )
          )
        )

        val expectedResult = PreviousRegistrationNew(
          country = Country("DE", "Germany"),
          previousSchemesDetails = Seq(
            PreviousSchemeDetails(
              previousScheme = PreviousScheme.OSSU,
              previousSchemeNumbers = PreviousSchemeNumbers(
                previousSchemeNumber = "DE123",
                previousIntermediaryNumber = Some("IN123")
              )
            )
          )
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[PreviousRegistrationNew] mustBe JsSuccess(expectedResult)
      }

      "when optional values are absent" in {

        val json = Json.obj(
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
        )

        val expectedResult = PreviousRegistrationNew(
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
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[PreviousRegistrationNew] mustBe JsSuccess(expectedResult)
      }
    }

    "when values are absent" in {

      val json = Json.obj()
      json.validate[PreviousRegistrationNew] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> 12345
        ),
        "previousSchemesDetails" -> Json.arr(
          Json.obj(
            "previousScheme" -> "ossu",
            "previousSchemeNumbers" -> Json.obj(
              "previousSchemeNumber" -> "DE123",
              "previousIntermediaryNumber" -> "IN123"
            )
          )
        )
      )
      json.validate[PreviousRegistrationNew] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> JsNull,
          "name" -> "Germany"
        ),
        "previousSchemesDetails" -> Json.arr(
          Json.obj(
            "previousScheme" -> "ossu",
            "previousSchemeNumbers" -> Json.obj(
              "previousSchemeNumber" -> "DE123",
              "previousIntermediaryNumber" -> "IN123"
            )
          )
        )
      )
      json.validate[PreviousRegistrationNew] mustBe a[JsError]
    }
  }

  "EncryptedPreviousRegistrationNew" - {

    "must deserialise/serialise to and from EncryptedPreviousRegistrationNew" - {

      "when all values are present" in {

        val json = Json.obj(
          "country" -> Json.obj(
            "code" -> "DE",
            "name" -> "Germany"
          ),
          "previousSchemeDetails" -> Json.arr(
            Json.obj(
              "previousScheme" -> "ossu",
              "previousSchemeNumbers" -> Json.obj(
                "previousSchemeNumber" -> "DE123",
                "previousIntermediaryNumber" -> "IN123"
              )
            )
          )
        )

        val expectedResult = EncryptedPreviousRegistrationNew(
          country = EncryptedCountry("DE", "Germany"),
          previousSchemeDetails = Seq(EncryptedPreviousSchemeDetails(
            previousScheme = "ossu",
            previousSchemeNumbers = EncryptedPreviousSchemeNumbers(
              previousSchemeNumber = "DE123",
              previousIntermediaryNumber = Some("IN123")
            )
          ))
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[EncryptedPreviousRegistrationNew] mustBe JsSuccess(expectedResult)
      }

      "when optional values are absent" in {

        val json = Json.obj(
          "country" -> Json.obj(
            "code" -> "DE",
            "name" -> "Germany"
          ),
          "previousSchemeDetails" -> Json.arr(
            Json.obj(
              "previousScheme" -> "ossu",
              "previousSchemeNumbers" -> Json.obj(
                "previousSchemeNumber" -> "DE123"
              )
            )
          )
        )

        val expectedResult = EncryptedPreviousRegistrationNew(
          country = EncryptedCountry("DE", "Germany"),
          previousSchemeDetails = Seq(EncryptedPreviousSchemeDetails(
            previousScheme = "ossu",
            previousSchemeNumbers = EncryptedPreviousSchemeNumbers(
              previousSchemeNumber = "DE123",
              previousIntermediaryNumber = None
            )
          ))
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[EncryptedPreviousRegistrationNew] mustBe JsSuccess(expectedResult)
      }
    }

    "when values are absent" in {

      val json = Json.obj()
      json.validate[EncryptedPreviousRegistrationNew] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> 12345,
          "name" -> "Germany"
        ),
        "previousSchemeDetails" -> Json.arr(
          Json.obj(
            "previousScheme" -> "ossu",
            "previousSchemeNumbers" -> Json.obj(
              "previousSchemeNumber" -> "DE123",
              "previousIntermediaryNumber" -> "IN123"
            )
          )
        )
      )
      json.validate[EncryptedPreviousRegistrationNew] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> JsNull
        ),
        "previousSchemeDetails" -> Json.arr(
          Json.obj(
            "previousScheme" -> "ossu",
            "previousSchemeNumbers" -> Json.obj(
              "previousSchemeNumber" -> "DE123",
              "previousIntermediaryNumber" -> "IN123"
            )
          )
        )
      )
      json.validate[EncryptedPreviousRegistrationNew] mustBe a[JsError]
    }
  }

  "PreviousRegistrationLegacy" - {

    "must deserialise/serialise to and from PreviousRegistrationLegacy" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "BE",
          "name" -> "Belgium"
        ),
        "vatNumber" -> "BE123"
      )

      val expectedResult = PreviousRegistrationLegacy(
        country = Country("BE", "Belgium"),
        vatNumber = "BE123"
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[PreviousRegistrationLegacy] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()
      json.validate[PreviousRegistrationLegacy] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> 12345,
          "name" -> "Belgium"
        ),
        "vatNumber" -> "BE123"
      )
      json.validate[PreviousRegistrationLegacy] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> JsNull,
          "name" -> "Belgium"
        ),
        "vatNumber" -> "BE123"
      )
      json.validate[PreviousRegistrationLegacy] mustBe a[JsError]
    }
  }

  "EncryptedPreviousRegistrationLegacy" - {

    "must deserialise/serialise to and from EncryptedPreviousRegistrationLegacy" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "BE",
          "name" -> "Belgium"
        ),
        "vatNumber" -> "BE123"
      )

      val expectedResult = EncryptedPreviousRegistrationLegacy(
        country = EncryptedCountry("BE", "Belgium"),
        vatNumber = "BE123"
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EncryptedPreviousRegistrationLegacy] mustBe JsSuccess(expectedResult)
    }

    "when values are absent" in {

      val json = Json.obj()
      json.validate[EncryptedPreviousRegistrationLegacy] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> 12345,
          "name" -> "Belgium"
        ),
        "vatNumber" -> "BE123"
      )
      json.validate[EncryptedPreviousRegistrationLegacy] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> JsNull,
          "name" -> "Belgium"
        ),
        "vatNumber" -> "BE123"
      )
      json.validate[EncryptedPreviousRegistrationLegacy] mustBe a[JsError]
    }
  }

  "PreviousSchemeDetails" - {

    "must deserialise/serialise to and from PreviousSchemeDetails" - {

      "when all values are present" in {
        val json = Json.obj(
          "previousScheme" -> "ossu",
          "previousSchemeNumbers" -> Json.obj(
            "previousSchemeNumber" -> "DE123",
            "previousIntermediaryNumber" -> "IN123"
          )
        )

        val expectedResult = PreviousSchemeDetails(
          previousScheme = PreviousScheme.OSSU,
          previousSchemeNumbers = PreviousSchemeNumbers(
            previousSchemeNumber = "DE123",
            previousIntermediaryNumber = Some("IN123")
          )
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[PreviousSchemeDetails] mustBe JsSuccess(expectedResult)
      }

      "when optional values are absent" in {

        val json = Json.obj(
          "previousScheme" -> "ossu",
          "previousSchemeNumbers" -> Json.obj(
            "previousSchemeNumber" -> "DE123"
          )
        )

        val expectedResult = PreviousSchemeDetails(
          previousScheme = PreviousScheme.OSSU,
          previousSchemeNumbers = PreviousSchemeNumbers(
            previousSchemeNumber = "DE123",
            previousIntermediaryNumber = None
          )
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[PreviousSchemeDetails] mustBe JsSuccess(expectedResult)
      }
    }

    "when values are absent" in {

      val json = Json.obj()
      json.validate[PreviousSchemeDetails] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "previousScheme" -> 12345,
        "previousSchemeNumbers" -> Json.obj(
          "previousSchemeNumber" -> "DE123",
          "previousIntermediaryNumber" -> "IN123"
        )
      )
      json.validate[PreviousSchemeDetails] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "previousScheme" -> JsNull,
        "previousSchemeNumbers" -> Json.obj(
          "previousSchemeNumber" -> "DE123",
          "previousIntermediaryNumber" -> "IN123"
        )
      )
      json.validate[PreviousSchemeDetails] mustBe a[JsError]
    }
  }

  "EncryptedPreviousSchemeDetails" - {

    "must deserialise/serialise to and from EncryptedPreviousSchemeDetails" - {

      "when all values are present" in {

        val json = Json.obj(
          "previousScheme" -> "ossu",
          "previousSchemeNumbers" -> Json.obj(
            "previousSchemeNumber" -> "DE123",
            "previousIntermediaryNumber" -> "IN123"
          )
        )

        val expectedResult = EncryptedPreviousSchemeDetails(
          previousScheme = "ossu",
          previousSchemeNumbers = EncryptedPreviousSchemeNumbers(
            previousSchemeNumber = "DE123",
            previousIntermediaryNumber = Some("IN123")
          )
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[EncryptedPreviousSchemeDetails] mustBe JsSuccess(expectedResult)
      }

      "when optional values are absent" in {

        val json = Json.obj(
          "previousScheme" -> "ossu",
          "previousSchemeNumbers" -> Json.obj(
            "previousSchemeNumber" -> "DE123",
          )
        )

        val expectedResult = EncryptedPreviousSchemeDetails(
          previousScheme = "ossu",
          previousSchemeNumbers = EncryptedPreviousSchemeNumbers(
            previousSchemeNumber = "DE123",
            previousIntermediaryNumber = None
          )
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[EncryptedPreviousSchemeDetails] mustBe JsSuccess(expectedResult)
      }
    }

    "when values are absent" in {

      val json = Json.obj()
      json.validate[EncryptedPreviousSchemeDetails] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "previousScheme" -> 12345,
        "previousSchemeNumbers" -> Json.obj(
          "previousSchemeNumber" -> "DE123",
          "previousIntermediaryNumber" -> "IN123"
        )
      )
      json.validate[EncryptedPreviousSchemeDetails] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "previousScheme" -> JsNull,
        "previousSchemeNumbers" -> Json.obj(
          "previousSchemeNumber" -> "DE123",
          "previousIntermediaryNumber" -> "IN123"
        )
      )
      json.validate[EncryptedPreviousSchemeDetails] mustBe a[JsError]
    }
  }

  "PreviousSchemeNumbers" - {

    "must deserialise/serialise to and from PreviousSchemeNumbers" - {

      "when all values are present" in {

        val json = Json.obj(
          "previousSchemeNumber" -> "DE123",
          "previousIntermediaryNumber" -> "IN123"
        )

        val expectedResult = PreviousSchemeNumbers(
          previousSchemeNumber = "DE123",
          previousIntermediaryNumber = Some("IN123")
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[PreviousSchemeNumbers] mustBe JsSuccess(expectedResult)
      }

      "when optional values are absent" in {

        val json = Json.obj(
          "previousSchemeNumber" -> "DE123"
        )

        val expectedResult = PreviousSchemeNumbers(
          previousSchemeNumber = "DE123",
          previousIntermediaryNumber = None
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[PreviousSchemeNumbers] mustBe JsSuccess(expectedResult)

      }
    }

    "when values are absent" in {

      val json = Json.obj()
      json.validate[PreviousSchemeNumbers] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "previousSchemeNumber" -> 12345,
        "previousIntermediaryNumber" -> "IN123"
      )
      json.validate[PreviousSchemeNumbers] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "previousSchemeNumber" -> JsNull,
        "previousIntermediaryNumber" -> "IN123"
      )
      json.validate[PreviousSchemeNumbers] mustBe a[JsError]
    }
  }

  "EncryptedPreviousSchemeNumbers" - {

    "must deserialise/serialise to and from EncryptedPreviousSchemeNumbers" - {

      "when all values are present" in {

        val json = Json.obj(
          "previousSchemeNumber" -> "DE123",
          "previousIntermediaryNumber" -> "IN123"
        )

        val expectedResult = EncryptedPreviousSchemeNumbers(
          previousSchemeNumber = "DE123",
          previousIntermediaryNumber = Some("IN123")
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[EncryptedPreviousSchemeNumbers] mustBe JsSuccess(expectedResult)
      }

      "when optional values are absent" in {
        val json = Json.obj(
          "previousSchemeNumber" -> "DE123"
        )

        val expectedResult = EncryptedPreviousSchemeNumbers(
          previousSchemeNumber = "DE123",
          previousIntermediaryNumber = None
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[EncryptedPreviousSchemeNumbers] mustBe JsSuccess(expectedResult)
      }
    }

    "when values are absent" in {

      val json = Json.obj()
      json.validate[EncryptedPreviousSchemeNumbers] mustBe a[JsError]
    }

    "when values are invalid" in {

      val json = Json.obj(
        "previousSchemeNumber" -> 12345,
        "previousIntermediaryNumber" -> "IN123"
      )
      json.validate[EncryptedPreviousSchemeNumbers] mustBe a[JsError]
    }

    "when values are null" in {

      val json = Json.obj(
        "previousSchemeNumber" -> JsNull,
        "previousIntermediaryNumber" -> "IN123"
      )
      json.validate[EncryptedPreviousSchemeNumbers] mustBe a[JsError]
    }
  }

}
