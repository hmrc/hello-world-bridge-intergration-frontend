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
import models.bridge.property.PropertyAssessment
import pages.*
import pages.property.*
import play.api.Logging
import play.api.libs.json.*
import models.RichJsObject

import scala.util.{Failure, Success}

class PropertyAssessmentUserAnswersService extends Logging {

  // ====================================================
  // Populate UserAnswers (unchanged responsibility)
  // ====================================================

  private def setIfEmpty[A](
                             answers: UserAnswers,
                             page: QuestionPage[A],
                             value: Option[A]
                           )(implicit reads: Reads[A], writes: Writes[A]): UserAnswers =
    (answers.get(page), value) match {

      case (None, Some(v)) =>
        answers.set(page, v) match {
          case Success(updated) => updated
          case Failure(e) =>
            logger.warn(s"Failed to auto-populate $page from assessment", e)
            answers
        }

      case _ =>
        answers
    }

  def populateFromAssessment(
                              existingAnswers: UserAnswers,
                              assessment: PropertyAssessment
                            ): UserAnswers = {

    val updates: List[UserAnswers => UserAnswers] = List(
      ua => setIfEmpty(ua, PropertyIdPage, Some(assessment.id)),
      ua => setIfEmpty(ua, PropertyIdxPage, Some(assessment.idx)),
      ua => setIfEmpty(ua, PropertyNamePage, assessment.name),
      ua => setIfEmpty(ua, PropertyLabelPage, Some(assessment.label)),
      ua => setIfEmpty(ua, PropertyDescriptionPage, assessment.description),
      ua => setIfEmpty(ua, PropertyOriginationPage, Some(assessment.origination)),
      ua => setIfEmpty(ua, PropertyTerminationPage, assessment.termination),
      ua => setIfEmpty(ua, PropertyCategoryCodePage, assessment.category.code),
      ua => setIfEmpty(ua, PropertyCategoryMeaningPage, assessment.category.meaning),
      ua => setIfEmpty(ua, PropertyTypeCodePage, assessment.`type`.code),
      ua => setIfEmpty(ua, PropertyTypeMeaningPage, assessment.`type`.meaning),
      ua => setIfEmpty(ua, PropertyClassCodePage, assessment.`class`.code),
      ua => setIfEmpty(ua, PropertyClassMeaningPage, assessment.`class`.meaning)
    )

    updates.foldLeft(existingAnswers)((ua, f) => f(ua))
  }

  // ====================================================
  // Merge UserAnswers back into ORIGINAL JSON
  // ====================================================

  def mergeIntoOriginalJson(
                             originalJson: JsValue,
                             answers: UserAnswers
                           ): JsValue = {

    originalJson match {
      case root: JsObject =>

        val updatedProperties =
          (root \ "properties").asOpt[JsArray].map { propertiesArray =>
            val updatedProps = propertiesArray.value.zipWithIndex.map {
              case (propObj: JsObject, propIndex) if propIndex == 0 =>

                val updatedData =
                  (propObj \ "data").asOpt[JsObject].map { dataObj =>
                    val updatedAssessments =
                      (dataObj \ "assessments").asOpt[JsArray].map { assessmentsArray =>
                        val updatedAssessmentsValues =
                          assessmentsArray.value.zipWithIndex.map {
                            case (assessmentObj: JsObject, assessmentIndex)
                              if assessmentIndex == 0 =>
                              mergeAssessmentFields(assessmentObj, answers)

                            case (other, _) => other
                          }

                        dataObj + ("assessments" -> JsArray(updatedAssessmentsValues))
                      }.getOrElse(dataObj)

                    dataObj ++ updatedAssessments
                  }.getOrElse(propObj)

                propObj + ("data" -> updatedData)

              case (other, _) => other
            }

            JsArray(updatedProps)
          }

        updatedProperties
          .map(arr => root + ("properties" -> arr))
          .getOrElse(originalJson)

      case _ =>
        originalJson
    }
  }

  private def mergeAssessmentFields(
                                     original: JsObject,
                                     answers: UserAnswers
                                   ): JsObject = {

    var updated = original

    updated =
      overrideString(updated, PropertyLabelPage, __ \ "label", answers)

    updated =
      overrideString(updated, PropertyDescriptionPage, __ \ "description", answers)

    updated =
      overrideString(updated, PropertyOriginationPage, __ \ "origination", answers)

    updated =
      overrideString(updated, PropertyTerminationPage, __ \ "termination", answers)

    updated =
      overrideString(updated, PropertyCategoryCodePage, __ \ "category" \ "code", answers)

    updated =
      overrideString(updated, PropertyCategoryMeaningPage, __ \ "category" \ "meaning", answers)

    updated =
      overrideString(updated, PropertyTypeCodePage, __ \ "type" \ "code", answers)

    updated =
      overrideString(updated, PropertyTypeMeaningPage, __ \ "type" \ "meaning", answers)

    updated =
      overrideString(updated, PropertyClassCodePage, __ \ "class" \ "code", answers)

    updated =
      overrideString(updated, PropertyClassMeaningPage, __ \ "class" \ "meaning", answers)

    updated
  }

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
}