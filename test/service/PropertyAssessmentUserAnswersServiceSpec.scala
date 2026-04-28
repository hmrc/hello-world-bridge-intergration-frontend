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
import pages.property.*
import play.api.libs.json.*

class PropertyAssessmentUserAnswersServiceSpec
  extends SpecBase
    with MockitoSugar
    with TestData {

  private val service = new PropertyAssessmentUserAnswersService



  // =========================================================
  // populateFromAssessment
  // =========================================================

  "populateFromAssessment" - {

    "populate empty UserAnswers from assessment" in {

      val result =
        service.populateFromAssessment(emptyUserAnswers, assessment)

      result.get(PropertyIdPage) mustBe Some(42)
      result.get(PropertyIdxPage) mustBe Some("PA")
      result.get(PropertyNamePage) mustBe Some("Assessment")
      result.get(PropertyLabelPage) mustBe Some("Label")
      result.get(PropertyDescriptionPage) mustBe Some("Desc")
      result.get(PropertyOriginationPage) mustBe Some("2021")
      result.get(PropertyCategoryCodePage) mustBe Some("PA")
      result.get(PropertyCategoryMeaningPage) mustBe Some("Cat1")
      result.get(PropertyTypeCodePage) mustBe Some("T1")
      result.get(PropertyTypeMeaningPage) mustBe Some("Type1")
      result.get(PropertyClassCodePage) mustBe Some("CL1")
      result.get(PropertyClassMeaningPage) mustBe Some("Class1")
    }

    "not overwrite existing answers" in {

      val existing =
        emptyUserAnswers
          .set(PropertyLabelPage, "Existing Label")
          .success.value

      val result =
        service.populateFromAssessment(existing, assessment)

      result.get(PropertyLabelPage) mustBe Some("Existing Label")
    }
  }

  // =========================================================
  // mergeIntoOriginalJson
  // =========================================================

  "mergeIntoOriginalJson" - {

    "merge assessment fields into the first assessment of the first property" in {

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
              "data" -> Json.obj(
                "assessments" -> Json.arr(
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
            )
          )
        )

      val result =
        service.mergeIntoOriginalJson(originalJson, answers)

      val assessmentJson =
        ((result \ "properties")(0) \ "data" \ "assessments")
          .as[JsArray]
          .value(0)
          .as[JsObject]

      (assessmentJson \ "label").as[String] mustBe "Updated Label"
      (assessmentJson \ "description").as[String] mustBe "Updated Description"
      (assessmentJson \ "type" \ "code").as[String] mustBe "NEW-TYPE"
      (assessmentJson \ "type" \ "meaning").as[String] mustBe "New Type"
    }

    "only update the first assessment and leave others unchanged" in {

      val answers =
        emptyUserAnswers
          .set(PropertyLabelPage, "Updated Label")
          .success.value

      val originalJson =
        Json.obj(
          "properties" -> Json.arr(
            Json.obj(
              "data" -> Json.obj(
                "assessments" -> Json.arr(
                  Json.obj("label" -> "First"),
                  Json.obj("label" -> "Second")
                )
              )
            )
          )
        )

      val result =
        service.mergeIntoOriginalJson(originalJson, answers)

      val assessments =
        ((result \ "properties")(0) \ "data" \ "assessments").as[JsArray]

      (assessments(0) \ "label").as[String] mustBe "Updated Label"
      (assessments(1) \ "label").as[String] mustBe "Second"
    }

    "return original JSON unchanged if properties array is missing" in {

      val originalJson =
        Json.obj("foo" -> "bar")

      val result =
        service.mergeIntoOriginalJson(originalJson, emptyUserAnswers)

      result mustBe originalJson
    }

    "return original JSON unchanged if not an object" in {

      val json = JsString("not-object")

      service.mergeIntoOriginalJson(json, emptyUserAnswers) mustBe json
    }

    "handle missing assessments array safely" in {

      val answers =
        emptyUserAnswers
          .set(PropertyLabelPage, "Updated")
          .success.value

      val originalJson =
        Json.obj(
          "properties" -> Json.arr(
            Json.obj(
              "data" -> Json.obj()
            )
          )
        )

      val result =
        service.mergeIntoOriginalJson(originalJson, answers)

      // no crash, structure preserved
      (result \ "properties").isDefined mustBe true
    }
  }
}
