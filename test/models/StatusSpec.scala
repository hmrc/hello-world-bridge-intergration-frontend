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

package models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

class StatusSpec extends AnyWordSpec with Matchers {

  "Status enum" must {

    "contain all expected values" in {
      Status.values must contain theSameElementsAs Seq(
        Status.Pending,
        Status.Rejected,
        Status.Approved
      )
    }

    "serialise Status values to JSON strings" in {
      Json.toJson(Status.Pending)  mustBe Json.toJson("Pending")
      Json.toJson(Status.Rejected) mustBe Json.toJson("Rejected")
      Json.toJson(Status.Approved) mustBe Json.toJson("Approved")
    }

    "deserialise JSON strings to Status values" in {
      Json.fromJson[Status](Json.toJson("Pending"))  mustBe JsSuccess(Status.Pending)
      Json.fromJson[Status](Json.toJson("Rejected")) mustBe JsSuccess(Status.Rejected)
      Json.fromJson[Status](Json.toJson("Approved")) mustBe JsSuccess(Status.Approved)
    }
  }
}