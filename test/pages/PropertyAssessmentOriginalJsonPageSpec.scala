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

package pages

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pages.property.PropertyAssessmentOriginalJsonPage
import play.api.libs.json.*

class PropertyAssessmentOriginalJsonPageSpec
  extends AnyWordSpec
    with Matchers {

  "PropertyAssessmentOriginalJsonPage" should {

    "be a QuestionPage of JsValue" in {
      val _: QuestionPage[JsValue] = PropertyAssessmentOriginalJsonPage
      succeed
    }

    "have the correct JSON path" in {
      PropertyAssessmentOriginalJsonPage.path mustBe
        (JsPath \ "propertyAssessmentOriginalJson")
    }

    "write a JsValue at the expected JSON path" in {
      val value = Json.obj("foo" -> "bar")

      val result =
        PropertyAssessmentOriginalJsonPage.path.json
          .put(value)
          .reads(Json.obj())
          .get

      result mustBe Json.obj(
        "propertyAssessmentOriginalJson" -> Json.obj("foo" -> "bar")
      )
    }

    "read a JsValue from the expected JSON path" in {
      val json =
        Json.obj(
          "propertyAssessmentOriginalJson" ->
            Json.obj("answer" -> 123)
        )

      val result =
        PropertyAssessmentOriginalJsonPage.path.json
          .pick
          .reads(json)

      result.isSuccess mustBe true
      result.get mustBe Json.obj("answer" -> 123)
    }

    "fail to read when the JSON path is missing" in {
      val json = Json.obj()

      val result =
        PropertyAssessmentOriginalJsonPage.path.json
          .pick
          .reads(json)

      result.isError mustBe true
    }
  }
}