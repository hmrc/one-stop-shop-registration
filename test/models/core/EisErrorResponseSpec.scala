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

package models.core


import base.BaseSpec
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}

import java.time.Instant

class EisErrorResponseSpec extends AnyFreeSpec with Matchers with BaseSpec {

  private val randomTimestamp = Instant.now()

  "EISErrorResponse" - {

    "must serialise and deserialise valid EISErrorResponse" in {

      val eisErrorResponse: EisErrorResponse =
        EisErrorResponse(
          timestamp = randomTimestamp,
          error = "OSS_001",
          errorMessage = "Invalid input"
        )

      val expectedJson = Json.obj(
        "timestamp" -> randomTimestamp,
        "error" -> "OSS_001",
        "errorMessage" -> "Invalid input"
      )

      Json.toJson(eisErrorResponse) mustEqual expectedJson
      expectedJson.validate[EisErrorResponse] mustEqual JsSuccess(eisErrorResponse)

    }
  }

  "EisDisplayErrorResponse" - {

    "must serialise and deserialise valid EisDisplayErrorResponse" in {

      val eisErrorResponse: EisDisplayErrorResponse =
        EisDisplayErrorResponse(
          errorDetail = EisDisplayErrorDetail(
            correlationId = "56523dfe-312d-4bbb-811d-8322c3d36abe",
            errorCode = "003",
            errorMessage = "Duplicate Acknowledgment Reference",
            timestamp = "2022-12-30T13:43:13Z"
          )
        )

      val expectedJson = Json.obj(
        "errorDetail" -> Json.obj(
          "correlationId" -> "56523dfe-312d-4bbb-811d-8322c3d36abe",
          "errorCode" -> "003",
          "errorMessage" -> "Duplicate Acknowledgment Reference",
          "timestamp" -> "2022-12-30T13:43:13Z"
        )
      )

      Json.toJson(eisErrorResponse) mustEqual expectedJson
      expectedJson.validate[EisDisplayErrorResponse] mustEqual JsSuccess(eisErrorResponse)
    }
  }

  "EisDisplayErrorDetail" - {

    "must serialise and deserialise valid EisDisplayErrorDetail" in {

      val eisErrorResponse: EisDisplayErrorDetail =
        EisDisplayErrorDetail(
          correlationId = "56523dfe-312d-4bbb-811d-8322c3d36abe",
          errorCode = "003",
          errorMessage = "Duplicate Acknowledgment Reference",
          timestamp = "2022-12-30T13:43:13Z"
        )

      val expectedJson = Json.obj(
        "correlationId" -> "56523dfe-312d-4bbb-811d-8322c3d36abe",
        "errorCode" -> "003",
        "errorMessage" -> "Duplicate Acknowledgment Reference",
        "timestamp" -> "2022-12-30T13:43:13Z",
      )

      Json.toJson(eisErrorResponse) mustEqual expectedJson
      expectedJson.validate[EisDisplayErrorDetail] mustEqual JsSuccess(eisErrorResponse)
    }
  }

}
