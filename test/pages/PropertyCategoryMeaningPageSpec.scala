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
import pages.property.PropertyCategoryMeaningPage
import play.api.libs.json.*

class PropertyCategoryMeaningPageSpec
  extends AnyWordSpec
    with Matchers {

  "PropertyCategoryMeaningPage" should {

    "be a QuestionPage of String" in {
      // Compile‑time type check
      val _: QuestionPage[String] = PropertyCategoryMeaningPage
      succeed
    }

    "have the correct string identifier" in {
      PropertyCategoryMeaningPage.toString mustBe "propertyCategoryMeaning"
    }

    "have the correct JSON path" in {
      PropertyCategoryMeaningPage.path mustBe
        (JsPath \ "propertyCategoryMeaning")
    }

    "write a String value at the expected JSON path" in {
      val value = "LTX-DOM-PRP"

      val result =
        PropertyCategoryMeaningPage.path.json
          .put(JsString(value))
          .reads(Json.obj())
          .get

      result mustBe Json.obj(
        "propertyCategoryMeaning" -> JsString(value)
      )
    }

    "read a String value from the expected JSON path" in {
      val json =
        Json.obj(
          "propertyCategoryMeaning" -> JsString("LTX-DOM-PRP")
        )

      val result =
        PropertyCategoryMeaningPage.path.json
          .pick
          .reads(json)

      result.isSuccess mustBe true
      result.get mustBe Json.toJson("LTX-DOM-PRP")
    }

    "fail to read when the JSON path is missing" in {
      val json = Json.obj()

      val result =
        PropertyCategoryMeaningPage.path.json
          .pick
          .reads(json)

      result.isError mustBe true
    }
  }
}