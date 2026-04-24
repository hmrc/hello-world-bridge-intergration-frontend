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


import models.UserAnswers
import models.bridge.relationhship.Relationship
import pages.QuestionPage
import pages.relationship.*
import play.api.Logging
import play.api.libs.json.*

class PropertyLinksUserAnswersService extends Logging {

  // ====================================================
  // Populate UserAnswers from Relationship (unchanged)
  // ====================================================

  private def setIfEmpty[A](
                             answers: UserAnswers,
                             page: QuestionPage[A],
                             value: Option[A]
                           )(implicit reads: Reads[A], writes: Writes[A]): UserAnswers =
    (answers.get(page), value) match {

      case (None, Some(v)) =>
        answers.set(page, v).getOrElse {
          logger.warn(s"Failed to auto-populate $page")
          answers
        }

      case _ =>
        answers
    }

  def populateFromRelationship(
                                existingAnswers: UserAnswers,
                                relationship: Relationship
                              ): UserAnswers = {

    val updates: List[UserAnswers => UserAnswers] = List(
      ua => setIfEmpty(ua, RelationshipIdPage,               relationship.id),
      ua => setIfEmpty(ua, RelationshipIdxPage,              Some(relationship.idx)),
      ua => setIfEmpty(ua, RelationshipNamePage,             Some(relationship.name)),
      ua => setIfEmpty(ua, RelationshipLabelPage,            Some(relationship.label)),
      ua => setIfEmpty(ua, RelationshipDescriptionPage,      Some(relationship.description)),
      ua => setIfEmpty(ua, RelationshipOriginationPage,      relationship.origination),
      ua => setIfEmpty(ua, RelationshipTerminationPage,      relationship.termination),
      ua => setIfEmpty(ua, RelationshipCategoryCodePage,     relationship.category.code),
      ua => setIfEmpty(ua, RelationshipCategoryMeaningPage,  relationship.category.meaning),
      ua => setIfEmpty(ua, RelationshipTypeCodePage,         relationship.`type`.code),
      ua => setIfEmpty(ua, RelationshipTypeMeaningPage,      relationship.`type`.meaning),
      ua => setIfEmpty(ua, RelationshipClassCodePage,        relationship.`class`.code),
      ua => setIfEmpty(ua, RelationshipClassMeaningPage,     relationship.`class`.meaning)
    )

    updates.foldLeft(existingAnswers)((ua, f) => f(ua))
  }

  // ====================================================
  // Build PATCH JSON from UserAnswers
  // ====================================================

  private def answersToPatch(ua: UserAnswers): JsObject = {

    val flatFields =
      Seq(
        "label"       -> ua.get(RelationshipLabelPage),
        "description" -> ua.get(RelationshipDescriptionPage),
        "name"        -> ua.get(RelationshipNamePage),
        "idx"         -> ua.get(RelationshipIdxPage)
      ).collect {
        case (k, Some(v)) => k -> JsString(v)
      }

    val categoryPatch =
      for {
        code    <- ua.get(RelationshipCategoryCodePage)
        meaning <- ua.get(RelationshipCategoryMeaningPage)
      } yield Json.obj(
        "category" -> Json.obj(
          "code"    -> code,
          "meaning" -> meaning
        )
      )

    val typePatch =
      for {
        code    <- ua.get(RelationshipTypeCodePage)
        meaning <- ua.get(RelationshipTypeMeaningPage)
      } yield Json.obj(
        "type" -> Json.obj(
          "code"    -> code,
          "meaning" -> meaning
        )
      )

    val classPatch =
      for {
        code    <- ua.get(RelationshipClassCodePage)
        meaning <- ua.get(RelationshipClassMeaningPage)
      } yield Json.obj(
        "class" -> Json.obj(
          "code"    -> code,
          "meaning" -> meaning
        )
      )

    Seq(
      JsObject(flatFields),
      categoryPatch.getOrElse(Json.obj()),
      typePatch.getOrElse(Json.obj()),
      classPatch.getOrElse(Json.obj())
    ).foldLeft(Json.obj())(_ deepMerge _)
  }

  // ====================================================
  // ✅ MERGE USER ANSWERS INTO RELATIONSHIP JSON (FIXED)
  // ====================================================

  /**
   * Merges UserAnswers into the ORIGINAL Relationship JSON.
   *
   * IMPORTANT:
   *  - originalJson MUST be a valid Relationship
   *  - This method guarantees mandatory fields stay intact
   *  - Property JSON will be rejected early
   */
  def mergeIntoOriginalJson(
                             originalJson: JsValue,
                             ua: UserAnswers
                           ): JsObject =

    originalJson match {

      // ✅ Valid Relationship JSON
      case relationship: JsObject
        if (relationship \ "data" \ "manifestations").isDefined =>

        relationship.deepMerge(answersToPatch(ua))

      // ❌ Property JSON mistakenly passed in
      case obj: JsObject
        if (obj \ "data" \ "assessments").isDefined =>

        throw new IllegalStateException(
          "Property JSON supplied where Relationship JSON was required"
        )

      // ❌ Anything else
      case other =>
        throw new IllegalStateException(
          s"Invalid JSON supplied to mergeIntoOriginalJson: ${Json.prettyPrint(other)}"
        )
    }
}