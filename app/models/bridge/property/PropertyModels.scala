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

package models.bridge.property

import models.bridge.common._
import play.api.libs.json.{JsValue, Json, OFormat}

case class AddressData(
                        property_full_address: Option[String],
                        address_line_1: Option[String],
                        address_postcode: Option[String],
                        known_as: Option[String]
                      )

object AddressData:
  implicit val format: OFormat[AddressData] = Json.format[AddressData]

case class LocationData(
                         local_authority_pseudo_area_code: Option[String],
                         ordnance_survey: Option[String],
                         google_maps: Option[String]
                       )

object LocationData:
  implicit val format: OFormat[LocationData] = Json.format[LocationData]

case class PropertyAssessment(
                               id: Long,
                               idx: String,
                               name: String,
                               label: String,
                               description: String,
                               origination: String,
                               termination: Option[String],
                               category: CodeMeaning,
                               `type`: CodeMeaning,
                               `class`: CodeMeaning,
                               data: PropertyAssessmentData,
                               protodata: List[ProtoData],
                               metadata: Metadata,
                               compartments: Map[String, String],
                               items: List[JsValue]
                             )

object PropertyAssessment:
  implicit val format: OFormat[PropertyAssessment] = Json.format[PropertyAssessment]


case class PropertyAssessmentData(
                                   foreign_ids: List[ForeignId],
                                   foreign_names: List[ForeignId],
                                   foreign_labels: List[ForeignId],
                                   property: PropertyReference,
                                   use: PropertyUse,
                                   valuation_surveys: List[JsValue], // unknown structure
                                   valuations: List[JsValue], // unknown structure
                                   valuation: ValuationData,
                                   list: ListData,
                                   workflow: WorkflowData
                                 )

object PropertyAssessmentData:
  implicit val format: OFormat[PropertyAssessmentData] = Json.format[PropertyAssessmentData]

case class PropertyReference(
                              property_id: Long,
                              cdb_property_id: Long
                            )

object PropertyReference:
  implicit val format: OFormat[PropertyReference] = Json.format[PropertyReference]

case class PropertyUse(
                        is_composite: Option[Boolean],
                        is_part_exempt: Option[Boolean],
                        use_description: Option[String]
                      )

object PropertyUse:
  implicit val format: OFormat[PropertyUse] = Json.format[PropertyUse]

case class ValuationData(
                          valuation_method_code: Option[String],
                          valuation_rateable: Option[String],
                          valuation_effective_date: Option[String]
                        )

object ValuationData:
  implicit val format: OFormat[ValuationData] = Json.format[ValuationData]

case class ListData(
                     list_category: Option[String],
                     list_function: Option[String],
                     list_year: Option[String],
                     list_authority_code: Option[String]
                   )
object ListData:
  implicit val format: OFormat[ListData] = Json.format[ListData]


case class WorkflowData(
                         cdb_job_id: Option[String]
                       )

object WorkflowData:
  implicit val format: OFormat[WorkflowData] = Json.format[WorkflowData]

case class PropertyData(
                         foreign_ids: List[ForeignId],
                         foreign_names: List[ForeignId],
                         foreign_labels: List[ForeignId],
                         addresses: AddressData,
                         location: LocationData,
                         assessments: List[PropertyAssessment]
                       )

object PropertyData:
  implicit val format: OFormat[PropertyData] = Json.format[PropertyData]


case class Property(
                     id: Long,
                     idx: String,
                     name: String,
                     label: String,
                     description: String,
                     origination: String,
                     termination: Option[String],
                     category: CodeMeaning,
                     `type`: CodeMeaning,
                     `class`: CodeMeaning,
                     data: PropertyData,
                     protodata: List[ProtoData],
                     metadata: Metadata,
                     compartments: Map[String, String],
                     items: List[JsValue]
                   )

object Property {
  implicit val format: OFormat[Property] = Json.format[Property]
}
