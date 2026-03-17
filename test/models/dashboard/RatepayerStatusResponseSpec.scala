/*
 * Copyright 2026 HM Revenue & Customs
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

package models.dashboard

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsSuccess, Json}

class RatepayerStatusResponseSpec extends AnyFreeSpec with Matchers {

  "RatepayerStatusResponse JSON format" - {

    val json =
      Json.parse(
        """
          |{
          |  "activeRatepayerPersonExists": true,
          |  "activeRatepayerPersonaExists": false,
          |  "activePropertyLinkCount": 3
          |}
          |""".stripMargin
      )

    val model =
      RatepayerStatusResponse(
        activeRatepayerPersonExists = true,
        activeRatepayerPersonaExists = false,
        activePropertyLinkCount = 3
      )

    "must deserialize valid JSON successfully" in {
      json.validate[RatepayerStatusResponse] mustBe JsSuccess(model)
    }

    "must serialize model to JSON successfully" in {
      Json.toJson(model) mustBe json
    }

    "must round‑trip to JSON and back" in {
      val jsonFromModel = Json.toJson(model)
      jsonFromModel.validate[RatepayerStatusResponse] mustBe JsSuccess(model)
    }

    "must fail when required fields are missing" in {
      val incompleteJson = Json.parse("""{ "activePropertyLinkCount": 1 }""")

      val result = incompleteJson.validate[RatepayerStatusResponse]

      result mustBe a[JsError]
    }

    "must fail when fields are the wrong type" in {
      val wrongTypeJson =
        Json.parse(
          """
            |{
            |  "activeRatepayerPersonExists": "yes",
            |  "activeRatepayerPersonaExists": false,
            |  "activePropertyLinkCount": "three"
            |}
            |""".stripMargin
        )

      val result = wrongTypeJson.validate[RatepayerStatusResponse]

      result mustBe a[JsError]
    }
  }
}
