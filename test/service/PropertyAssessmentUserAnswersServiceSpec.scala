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

package service

import base.SpecBase
import helpers.TestData
import org.scalatestplus.mockito.MockitoSugar
import pages.property._
import play.api.libs.json._

class PropertyAssessmentUserAnswersServiceSpec
  extends SpecBase
    with MockitoSugar
    with TestData {

  private val service = new PropertyAssessmentUserAnswersService

  // =========================================================
  // populateFromProperty
  // =========================================================

  "populateFromProperty" - {

    "populate empty UserAnswers from property" in {

      val result =
        service.populateFromProperty(emptyUserAnswers, testProperty)

      result.get(PropertyIdPage) mustBe Some(777)
      result.get(PropertyIdxPage) mustBe Some("PROP")
      result.get(PropertyNamePage) mustBe Some("Main Property")
      result.get(PropertyLabelPage) mustBe Some("Main Label")
      result.get(PropertyDescriptionPage) mustBe Some("Property description")
      result.get(PropertyOriginationPage) mustBe Some("2020")
    }

    "not overwrite existing answers" in {

      val existing =
        emptyUserAnswers
          .set(PropertyLabelPage, "Existing Label")
          .success
          .value

      val result =
        service.populateFromProperty(existing, testProperty)

      result.get(PropertyLabelPage) mustBe Some("Existing Label")
    }
  }

  // =========================================================
  // mergeIntoOriginalJson
  // =========================================================

  "mergeIntoOriginalJson" - {

    "merge property fields into the first property only" in {

      val answers =
        emptyUserAnswers
          .set(PropertyLabelPage, "Updated Label").success.value
          .set(PropertyDescriptionPage, "Updated Description").success.value
          .set(PropertyTypeCodePage, "NEW-TYPE").success.value
          .set(PropertyTypeMeaningPage, "New Type").success.value

      val originalJson =
        Json.obj(
          "properties" -> Json.arr(
            Json.obj(
              "label" -> "Old Label",
              "description" -> "Old Description",
              "type" -> Json.obj(
                "code" -> "OLD",
                "meaning" -> "Old Type"
              )
            )
          )
        )

      val result =
        service.mergeIntoOriginalJson(originalJson, answers)

      val propertyJson =
        (result \ "properties")(0).as[JsObject]

      (propertyJson \ "label").as[String] mustBe "Updated Label"
      (propertyJson \ "description").as[String] mustBe "Updated Description"
      (propertyJson \ "type" \ "code").as[String] mustBe "NEW-TYPE"
      (propertyJson \ "type" \ "meaning").as[String] mustBe "New Type"
    }

    "only update the first property and leave others unchanged" in {

      val answers =
        emptyUserAnswers
          .set(PropertyLabelPage, "Updated Label")
          .success
          .value

      val originalJson =
        Json.obj(
          "properties" -> Json.arr(
            Json.obj("label" -> "First"),
            Json.obj("label" -> "Second")
          )
        )

      val result =
        service.mergeIntoOriginalJson(originalJson, answers)

      val properties =
        (result \ "properties").as[JsArray]

      (properties(0) \ "label").as[String] mustBe "Updated Label"
      (properties(1) \ "label").as[String] mustBe "Second"
    }

    "return original JSON unchanged if properties array is missing" in {

      val originalJson =
        Json.obj("foo" -> "bar")

      val result =
        service.mergeIntoOriginalJson(originalJson, emptyUserAnswers)

      result mustBe originalJson
    }

    "return original JSON unchanged if root is not an object" in {

      val json = JsString("not-object")

      service.mergeIntoOriginalJson(json, emptyUserAnswers) mustBe json
    }

    "handle missing data or addresses safely" in {

      val answers =
        emptyUserAnswers
          .set(PropertyLabelPage, "Updated")
          .success
          .value

      val originalJson =
        Json.obj(
          "properties" -> Json.arr(
            Json.obj(
              "label" -> "Original"
              // no data / no addresses
            )
          )
        )

      val result =
        service.mergeIntoOriginalJson(originalJson, answers)

      ((result \ "properties")(0) \ "label").as[String] mustBe "Updated"
    }
  }
}