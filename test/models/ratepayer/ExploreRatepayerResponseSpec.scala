/*
 * Copyright 2025 HM Revenue & Customs
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

package models.ratepayer

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import models.ratepayer.ExploreRatePayerResponse

class ExploreRatepayerResponseSpec extends AnyWordSpec with Matchers {

  "ExploreRatePayerResponse.format" should {

    "serialise to JSON with an empty person array" in {
      val model = ExploreRatePayerResponse(persons = Nil)

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "persons" -> Json.arr()
      )
    }

    "deserialise to JSON with an empty person array" in {
      val json = Json.obj("persons" -> Json.arr())

      val model = json.as[ExploreRatePayerResponse]

      model mustBe ExploreRatePayerResponse(persons = Nil)
    }

    "writes then reads correctly for empty persons list" in {
      val original = ExploreRatePayerResponse(persons = Nil)

      val json = Json.toJson(original)
      val parsed = json.as[ExploreRatePayerResponse]

      parsed mustBe original
    }
  }
}

