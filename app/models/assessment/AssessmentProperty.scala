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

package models.assessment

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class AssessmentProperty(
                               address: String,
                               foreignId: String,
                               laCode: String,
                               description: String,
                               rateableValue: Int
                             )

object AssessmentProperty {

  implicit val reads: Reads[AssessmentProperty] = (
    (__ \ "data" \ "addresses" \ "property_full_address").read[String] and

      (__ \ "data" \ "foreign_ids").read[List[JsValue]].map { ids =>
        ids.find(obj => (obj \ "system").asOpt[String].contains("National_Address_Gazetteer"))
          .flatMap(obj => (obj \ "value").asOpt[String])
          .getOrElse("")
      } and

      (__ \ "data" \ "location" \ "local_authority_pseudo_area_code").read[String] and
      (__ \ "data" \ "assessments").readNullable[List[JsValue]].map { opt =>
        opt
          .flatMap(_.headOption.flatMap(a =>
            (a \ "data" \ "use" \ "use_description").asOpt[String]
          ))
          .getOrElse("")
      } and

      Reads[Int] { json =>
        (json \ "valuation" \ "valuation_rateable").validate[Int] orElse
          (json \ "data" \ "assessments").validateOpt[List[JsValue]].flatMap {
            case Some(assessments) =>
              assessments.headOption
                .flatMap(a => (a \ "data" \ "valuation" \ "valuation_rateable").asOpt[Int])
                .map(JsSuccess(_))
                .getOrElse(JsSuccess(0))
            case None => JsSuccess(0)
          }
      }
    )(AssessmentProperty.apply _)


  implicit val writes: OWrites[AssessmentProperty] = Json.writes[AssessmentProperty]
}


