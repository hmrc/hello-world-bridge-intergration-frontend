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

import models.{RichJsObject, UserAnswers}
import models.bridge.relationhship.Relationship
import pages.QuestionPage
import pages.relationship.*
import play.api.Logging
import play.api.libs.json.*

class PropertyLinksUserAnswersService extends ServiceHelper {

  def populateFromRelationship(
                                existingAnswers: UserAnswers,
                                relationship: Relationship
                              ): UserAnswers = {

    val updates: List[UserAnswers => UserAnswers] = List(
      ua => setIfEmpty(ua, RelationshipIdPage,              relationship.id),
      ua => setIfEmpty(ua, RelationshipIdxPage,             Some(relationship.idx)),
      ua => setIfEmpty(ua, RelationshipNamePage,            Some(relationship.name)),
      ua => setIfEmpty(ua, RelationshipLabelPage,           Some(relationship.label)),
      ua => setIfEmpty(ua, RelationshipDescriptionPage,     Some(relationship.description)),
      ua => setIfEmpty(ua, RelationshipOriginationPage,     relationship.origination),
      ua => setIfEmpty(ua, RelationshipTerminationPage,     relationship.termination),
      ua => setIfEmpty(ua, RelationshipCategoryCodePage,    relationship.category.code),
      ua => setIfEmpty(ua, RelationshipCategoryMeaningPage, relationship.category.meaning),
      ua => setIfEmpty(ua, RelationshipTypeCodePage,        relationship.`type`.code),
      ua => setIfEmpty(ua, RelationshipTypeMeaningPage,     relationship.`type`.meaning),
      ua => setIfEmpty(ua, RelationshipClassCodePage,       relationship.`class`.code),
      ua => setIfEmpty(ua, RelationshipClassMeaningPage,    relationship.`class`.meaning)
    )

    updates.foldLeft(existingAnswers)((ua, f) => f(ua))
  }

  // ====================================================
  // Safe JSON Overrides (DATA ONLY)
  // ====================================================

  private def overrideString(
                              json: JsObject,
                              page: QuestionPage[String],
                              path: JsPath,
                              answers: UserAnswers
                            ): JsObject =
    answers.get(page) match {
      case Some(value) =>
        json.setObject(path, JsString(value)).getOrElse(json)
      case None =>
        json
    }

  private def mergeRelationshipData(
                                     data: JsObject,
                                     answers: UserAnswers
                                   ): JsObject = {

    var updated = data

    updated = overrideString(updated, RelationshipLabelPage,       __ \ "label", answers)
    updated = overrideString(updated, RelationshipDescriptionPage, __ \ "description", answers)
    updated = overrideString(updated, RelationshipNamePage,        __ \ "name", answers)

    updated = overrideString(updated, RelationshipCategoryCodePage,    __ \ "category" \ "code", answers)
    updated = overrideString(updated, RelationshipCategoryMeaningPage, __ \ "category" \ "meaning", answers)

    updated
  }

  // ====================================================
  // Item Update (data only)
  // ====================================================

  private def updateItems(
                           compartment: JsObject,
                           answers: UserAnswers
                         ): JsArray =
    (compartment \ "items").asOpt[JsArray] match {
      case Some(items) =>
        JsArray(
          items.value.map {
            case item: JsObject =>
              val data = (item \ "data").asOpt[JsObject].getOrElse(Json.obj())
              item + ("data" -> mergeRelationshipData(data, answers))
            case other => other
          }
        )

      case None =>
        JsArray.empty
    }

  private def mergeRelationshipRoot(
                                     relationship: JsObject,
                                     answers: UserAnswers
                                   ): JsObject = {
    var updated = relationship

    updated = overrideString(updated, RelationshipLabelPage, __ \ "label", answers)
    updated = overrideString(updated, RelationshipNamePage, __ \ "name", answers)
    updated = overrideString(updated, RelationshipDescriptionPage, __ \ "description", answers)
    updated = overrideString(updated, RelationshipCategoryCodePage, __ \ "category" \ "code", answers)
    updated = overrideString(updated, RelationshipCategoryMeaningPage, __ \ "category" \ "meaning", answers)
    updated = overrideString(updated, RelationshipTypeCodePage, __ \ "type" \ "code", answers)
    updated = overrideString(updated, RelationshipTypeMeaningPage, __ \ "type" \ "meaning", answers)
    updated = overrideString(updated, RelationshipClassCodePage, __ \ "class" \ "code", answers)
    updated = overrideString(updated, RelationshipClassMeaningPage, __ \ "class" \ "meaning", answers)

    updated
  }

  def mergeIntoOriginalJson(
                             originalJson: JsValue,
                             answers: UserAnswers
                           ): JsValue = originalJson match {

    case relationship: JsObject =>

      val updatedRoot =
        mergeRelationshipRoot(relationship, answers)

      val updatedItems =
        updateItems(updatedRoot, answers)

      updatedRoot + ("items" -> updatedItems)

    case other =>
      other
  }
}