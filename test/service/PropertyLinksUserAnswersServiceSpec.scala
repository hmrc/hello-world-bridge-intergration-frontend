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
import models.UserAnswers
import models.bridge.relationhship.*
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import pages.relationship.*
import play.api.libs.json.*
import play.api.test.Helpers.*
import models.bridge.common.*

class PropertyLinksUserAnswersServiceSpec
  extends SpecBase
    with MockitoSugar {

  private val service = new PropertyLinksUserAnswersService

  private val relationship =
    Relationship(
      id = Some(13),
      idx = "IDX-1",
      name = "Property Link",
      label = "Ratepayer-Property",
      description = "Test description",
      origination = None,
      termination = None,
      category = CodeMeaning(Some("CAT"), Some("Category")),
      `type` = CodeMeaning(Some("TYPE"), Some("Type")),
      `class` = CodeMeaning(Some("CLASS"), Some("Class")),
      data = RelationshipData(
        foreign_ids = Nil,
        foreign_names = Nil,
        foreign_labels = Nil,
        manifestations = Nil
      ),
      protodata = Nil,
      metadata = Metadata(
        sending = SendingMetadata(extracting = MetadataStage(), transforming = MetadataStage(), loading = MetadataStage()),
        receiving = ReceivingMetadata(unloading = MetadataStage(), transforming = MetadataStage(), storing = MetadataStage())
      ),
      compartments = Map.empty,
      items = Nil
    )

  // =========================================================
  // populateFromRelationship
  // =========================================================

  "populateFromRelationship" - {

    "populate empty UserAnswers with relationship values" in {

      val result =
        service.populateFromRelationship(emptyUserAnswers, relationship)

      result.get(RelationshipIdPage) mustBe Some(13)
      result.get(RelationshipIdxPage) mustBe Some("IDX-1")
      result.get(RelationshipNamePage) mustBe Some("Property Link")
      result.get(RelationshipLabelPage) mustBe Some("Ratepayer-Property")
      result.get(RelationshipDescriptionPage) mustBe Some("Test description")
      result.get(RelationshipCategoryCodePage) mustBe Some("CAT")
      result.get(RelationshipCategoryMeaningPage) mustBe Some("Category")
      result.get(RelationshipTypeCodePage) mustBe Some("TYPE")
      result.get(RelationshipTypeMeaningPage) mustBe Some("Type")
      result.get(RelationshipClassCodePage) mustBe Some("CLASS")
      result.get(RelationshipClassMeaningPage) mustBe Some("Class")
    }

    "not overwrite existing answers" in {

      val existing =
        emptyUserAnswers
          .set(RelationshipNamePage, "Existing Name")
          .success.value

      val result =
        service.populateFromRelationship(existing, relationship)

      result.get(RelationshipNamePage) mustBe Some("Existing Name")
    }
  }

  // =========================================================
  // mergeIntoOriginalJson
  // =========================================================

  "mergeIntoOriginalJson" - {

    "merge relationship root fields from UserAnswers" in {

      val answers =
        emptyUserAnswers
          .set(RelationshipNamePage, "Updated Name").success.value
          .set(RelationshipLabelPage, "Updated Label").success.value
          .set(RelationshipDescriptionPage, "Updated Description").success.value
          .set(RelationshipCategoryCodePage, "NEW-CAT").success.value
          .set(RelationshipCategoryMeaningPage, "New Category").success.value

      val originalJson =
        Json.obj(
          "name" -> "Original Name",
          "label" -> "Original Label",
          "description" -> "Original Description",
          "category" -> Json.obj(
            "code" -> "OLD",
            "meaning" -> "Old Category"
          ),
          "items" -> Json.arr()
        )

      val result =
        service.mergeIntoOriginalJson(originalJson, answers).as[JsObject]

      (result \ "name").as[String] mustBe "Updated Name"
      (result \ "label").as[String] mustBe "Updated Label"
      (result \ "description").as[String] mustBe "Updated Description"
      (result \ "category" \ "code").as[String] mustBe "NEW-CAT"
      (result \ "category" \ "meaning").as[String] mustBe "New Category"
    }

    "merge relationship data inside items" in {

      val answers =
        emptyUserAnswers
          .set(RelationshipNamePage, "Item Name").success.value
          .set(RelationshipLabelPage, "Item Label").success.value

      val originalJson =
        Json.obj(
          "name" -> "Root",
          "items" -> Json.arr(
            Json.obj(
              "data" -> Json.obj(
                "name" -> "Old Item Name",
                "label" -> "Old Item Label"
              )
            )
          )
        )

      val result =
        service.mergeIntoOriginalJson(originalJson, answers)

      val itemData =
        (result \ "items")(0) \ "data"

      (itemData \ "name").as[String] mustBe "Item Name"
      (itemData \ "label").as[String] mustBe "Item Label"
    }

    "return original JSON unchanged if not an object" in {

      val json = JsString("not-an-object")

      service.mergeIntoOriginalJson(json, emptyUserAnswers) mustBe json
    }

    "ignore missing items array safely" in {

      val answers =
        emptyUserAnswers
          .set(RelationshipNamePage, "Updated").success.value

      val originalJson =
        Json.obj(
          "name" -> "Original"
        )

      val result =
        service.mergeIntoOriginalJson(originalJson, answers)

      (result \ "name").as[String] mustBe "Updated"
      (result \ "items").as[JsArray].value mustBe empty
    }
  }
}