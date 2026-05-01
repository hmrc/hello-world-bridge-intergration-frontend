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
import models.bridge.property.Property
import pages._
import pages.property._
import play.api.libs.json._
import models.RichJsObject

class PropertyAssessmentUserAnswersService extends ServiceHelper {

  // ====================================================
  // Address merge helpers
  // ====================================================

  private def mergeAddressFields(
                                  original: JsObject,
                                  answers: UserAnswers
                                ): JsObject = {

    var updated = original

    updated = overrideString(updated, PropertyFullAddressPage, __ \ "property_full_address", answers)
    updated = overrideString(updated, PropertyAddressLine1Page, __ \ "address_line_1", answers)
    updated = overrideString(updated, PropertyAddressPostcodePage, __ \ "address_postcode", answers)
    updated = overrideString(updated, PropertyKnownAsPage, __ \ "known_as", answers)

    updated
  }

  // ====================================================
  // Populate UserAnswers from Property
  // ====================================================

  def populateFromProperty(
                            existingAnswers: UserAnswers,
                            property: Property
                          ): UserAnswers = {

    val updates: List[UserAnswers => UserAnswers] = List(
      ua => setIfEmpty(ua, PropertyIdPage, property.id),
      ua => setIfEmpty(ua, PropertyIdxPage, property.idx),
      ua => setIfEmpty(ua, PropertyNamePage, property.name),
      ua => setIfEmpty(ua, PropertyLabelPage, property.label),
      ua => setIfEmpty(ua, PropertyDescriptionPage, property.description),
      ua => setIfEmpty(ua, PropertyOriginationPage, property.origination),
      ua => setIfEmpty(ua, PropertyTerminationPage, property.termination),

      ua => setIfEmpty(
        ua,
        PropertyCategoryCodePage,
        property.category.flatMap(_.code).filter(_.nonEmpty)
      ),
      ua => setIfEmpty(
        ua,
        PropertyCategoryMeaningPage,
        property.category.flatMap(_.meaning).filter(_.nonEmpty)
      ),

      ua => setIfEmpty(
        ua,
        PropertyTypeCodePage,
        property.`type`.flatMap(_.code).filter(_.nonEmpty)
      ),
      ua => setIfEmpty(
        ua,
        PropertyTypeMeaningPage,
        property.`type`.flatMap(_.meaning).filter(_.nonEmpty)
      ),

      ua => setIfEmpty(
        ua,
        PropertyClassCodePage,
        property.`class`.flatMap(_.code).filter(_.nonEmpty)
      ),
      ua => setIfEmpty(
        ua,
        PropertyClassMeaningPage,
        property.`class`.flatMap(_.meaning).filter(_.nonEmpty)
      ),

      // Address fields
      ua => setIfEmpty(
        ua,
        PropertyFullAddressPage,
        property.data.flatMap(_.addresses.property_full_address)
      ),
      ua => setIfEmpty(
        ua,
        PropertyAddressLine1Page,
        property.data.flatMap(_.addresses.address_line_1)
      ),
      ua => setIfEmpty(
        ua,
        PropertyAddressPostcodePage,
        property.data.flatMap(_.addresses.address_postcode)
      ),
      ua => setIfEmpty(
        ua,
        PropertyKnownAsPage,
        property.data.flatMap(_.addresses.known_as)
      )
    )

    updates.foldLeft(existingAnswers)((ua, f) => f(ua))
  }

  // ====================================================
  // JSON override helpers
  // ====================================================

  private def overrideString(
                              json: JsObject,
                              page: QuestionPage[String],
                              path: JsPath,
                              answers: UserAnswers
                            ): JsObject =
    answers.get(page)
      .map(value => json.setObject(path, JsString(value)).getOrElse(json))
      .getOrElse(json)

  private def overrideLong(
                            json: JsObject,
                            page: QuestionPage[Long],
                            path: JsPath,
                            answers: UserAnswers
                          ): JsObject =
    answers.get(page)
      .map(value => json.setObject(path, JsNumber(value)).getOrElse(json))
      .getOrElse(json)

  // ====================================================
  // Merge UserAnswers BACK INTO PropertyPayload JSON
  // ====================================================

  def mergeIntoOriginalJson(
                             originalJson: JsValue,
                             answers: UserAnswers
                           ): JsValue = {

    originalJson match {
      case root: JsObject =>
        (root \ "properties").asOpt[JsArray] match {
          case Some(properties) =>
            val updatedProperties =
              properties.value.zipWithIndex.map {
                case (propObj: JsObject, 0) =>
                  mergePropertyFields(propObj, answers)
                case (other, _) =>
                  other
              }

            Json.obj("properties" -> JsArray(updatedProperties))

          case None =>
            root
        }

      case _ =>
        originalJson
    }
  }

  // ====================================================
  // Merge ALL editable Property fields
  // ====================================================

  private def mergePropertyFields(
                                   original: JsObject,
                                   answers: UserAnswers
                                 ): JsObject = {

    var updated = original

    // ─────────────────────────────
    // Property top-level fields
    // ─────────────────────────────

    updated = overrideLong(updated, PropertyIdPage, __ \ "id", answers)
    updated = overrideString(updated, PropertyIdxPage, __ \ "idx", answers)
    updated = overrideString(updated, PropertyNamePage, __ \ "name", answers)
    updated = overrideString(updated, PropertyLabelPage, __ \ "label", answers)
    updated = overrideString(updated, PropertyDescriptionPage, __ \ "description", answers)
    updated = overrideString(updated, PropertyOriginationPage, __ \ "origination", answers)
    updated = overrideString(updated, PropertyTerminationPage, __ \ "termination", answers)

    updated = overrideString(updated, PropertyCategoryCodePage, __ \ "category" \ "code", answers)
    updated = overrideString(updated, PropertyCategoryMeaningPage, __ \ "category" \ "meaning", answers)
    updated = overrideString(updated, PropertyTypeCodePage, __ \ "type" \ "code", answers)
    updated = overrideString(updated, PropertyTypeMeaningPage, __ \ "type" \ "meaning", answers)
    updated = overrideString(updated, PropertyClassCodePage, __ \ "class" \ "code", answers)
    updated = overrideString(updated, PropertyClassMeaningPage, __ \ "class" \ "meaning", answers)

    // ─────────────────────────────
    // Address (data.addresses) — VALIDATED
    // ─────────────────────────────

    updated =
      (updated \ "data").asOpt[JsObject] match {
        case Some(dataObj) =>
          (dataObj \ "addresses").asOpt[JsObject] match {
            case Some(addressesObj) =>
              val mergedAddresses = mergeAddressFields(addressesObj, answers)
              updated + ("data" -> (dataObj + ("addresses" -> mergedAddresses)))
            case None =>
              updated
          }
        case None =>
          updated
      }

    updated
  }
}