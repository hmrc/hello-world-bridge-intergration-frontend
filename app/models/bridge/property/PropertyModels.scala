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

import models.bridge.common.*
import play.api.libs.json.*

case class AddressData(
                        property_full_address: Option[String],
                        address_line_1: Option[String],
                        address_postcode: Option[String],
                        known_as: Option[String]
                      )
object AddressData {
  implicit val reads: Reads[AddressData] = Json.reads[AddressData]

  implicit val writes: OWrites[AddressData] = OWrites { data =>
    Json.obj(
      "property_full_address" -> data.property_full_address.map(JsString.apply).getOrElse(JsNull),
      "address_line_1" -> data.address_line_1.map(JsString.apply).getOrElse(JsNull),
      "address_postcode" -> data.address_postcode.map(JsString.apply).getOrElse(JsNull),
      "known_as" -> data.known_as.map(JsString.apply).getOrElse(JsNull)
    )
  }
}

case class LocationData(
                         local_authority_pseudo_area_code: Option[String],
                         ordnance_survey: Option[String],
                         google_maps: Option[String]
                       )
object LocationData {
  implicit val reads: Reads[LocationData] = Json.reads[LocationData]

  implicit val writes: OWrites[LocationData] = OWrites { data =>
    Json.obj(
      "local_authority_pseudo_area_code" -> data.local_authority_pseudo_area_code.map(JsString.apply).getOrElse(JsNull),
      "ordnance_survey" -> data.ordnance_survey.map(JsString.apply).getOrElse(JsNull),
      "google_maps" -> data.google_maps.map(JsString.apply).getOrElse(JsNull),
    )
  }
}


// =======================================================
// Facility / Artifact Value Structures
// =======================================================

case class SourceValue(
                        source: Option[String],
                        value: Option[String]
                      )
object SourceValue {
  implicit val format: OFormat[SourceValue] = Json.format[SourceValue]
}

case class QuantitySourceValue(
                                source: Option[String],
                                value: Long
                              )
object QuantitySourceValue {
  implicit val format: OFormat[QuantitySourceValue] = Json.format[QuantitySourceValue]
}

case class FacilityRecord(
                           activity: SourceValue,
                           code: SourceValue,
                           description: SourceValue,
                           quantity: QuantitySourceValue,
                           units: SourceValue
                         )
object FacilityRecord {
  implicit val format: OFormat[FacilityRecord] = Json.format[FacilityRecord]
}


// =======================================================
// ArtifactRecord (same shape as FacilityRecord)
// =======================================================

case class ArtifactRecord(
                           activity: SourceValue,
                           code: SourceValue,
                           description: SourceValue,
                           quantity: QuantitySourceValue,
                           units: SourceValue
                         )
object ArtifactRecord {
  implicit val format: OFormat[ArtifactRecord] = Json.format[ArtifactRecord]
}


// =======================================================
// SurveyLevelItemData (STRICT A1 representation of data:{...})
// =======================================================

case class SurveyLevelItemData(
                                foreign_ids: List[ForeignId],
                                foreign_names: List[ForeignId],
                                foreign_labels: List[ForeignId],
                                uses: List[JsObject],            // empty lists in your JSON
                                constructions: List[JsObject],   // empty lists in your JSON
                                facilities: List[FacilityRecord],
                                artifacts: List[ArtifactRecord],
                                uninheritances: List[JsObject],
                                attributions: List[JsObject]
                              )
object SurveyLevelItemData {
  implicit val format: OFormat[SurveyLevelItemData] = Json.format[SurveyLevelItemData]
}


// =======================================================
// SurveyLevelItem (recursive items[] structure)
// =======================================================

case class SurveyLevelItem(
                            id: Long,
                            idx: String,
                            name: Option[String],
                            label: String,
                            description: Option[String],
                            origination: String,
                            termination: Option[String],
                            category: CodeMeaning,
                            `type`: CodeMeaning,
                            `class`: CodeMeaning,
                            data: SurveyLevelItemData,
                            protodata: List[ProtoData],
                            metadata: Metadata,
                            compartments: Map[String, String],
                            items: List[SurveyLevelItem]     // recursive
                          )
object SurveyLevelItem {
  implicit val reads: Reads[SurveyLevelItem] = Json.reads[SurveyLevelItem]
  implicit val writes: OWrites[SurveyLevelItem] = Json.writes[SurveyLevelItem]
  implicit val format: OFormat[SurveyLevelItem] = OFormat(reads, writes)
}


// =======================================================
// PropertyAssessment Models
// =======================================================

case class PropertyReference(
                              property_id: Long,
                              cdb_property_id: Long
                            )
object PropertyReference {
  implicit val format: OFormat[PropertyReference] = Json.format[PropertyReference]
}

case class PropertyUse(
                        is_composite: Option[String],
                        is_part_exempt: Option[String],
                        use_description: Option[String]
                      )
object PropertyUse {
  implicit val format: OFormat[PropertyUse] = Json.format[PropertyUse]
}

case class ValuationData(
                          valuation_method_code: Option[String],
                          valuation_rateable: Option[Long],
                          valuation_effective_date: Option[String]
                        )
object ValuationData {
  implicit val format: OFormat[ValuationData] = Json.format[ValuationData]
}

case class ListData(
                     list_category: Option[String],
                     list_function: Option[String],
                     list_year: Option[String],
                     list_authority_code: Option[String]
                   )
object ListData {
  implicit val format: OFormat[ListData] = Json.format[ListData]
}

case class WorkflowData(
                         cdb_job_id: Option[Long]
                       )
object WorkflowData {
  implicit val format: OFormat[WorkflowData] = Json.format[WorkflowData]
}

case class PropertyAssessmentData(
                                   foreign_ids: List[ForeignId],
                                   foreign_names: List[ForeignId],
                                   foreign_labels: List[ForeignId],
                                   property: PropertyReference,
                                   use: PropertyUse,
                                   valuation_surveys: List[JsObject],   // full valuation survey model not requested here
                                   valuations: List[JsObject],
                                   valuation: ValuationData,
                                   list: ListData,
                                   workflow: WorkflowData
                                 )
object PropertyAssessmentData {
  implicit val format: OFormat[PropertyAssessmentData] = Json.format[PropertyAssessmentData]
}

final case class PropertyAssessmentContext(
                                            originalJson: JsValue,
                                            assessment: PropertyAssessment
                                          )

case class PropertyAssessment(
                               id: Long,
                               idx: String,
                               name: Option[String],
                               label: String,
                               description: Option[String],
                               origination: String,
                               termination: Option[String],
                               category: CodeMeaning,
                               `type`: CodeMeaning,
                               `class`: CodeMeaning,
                               data: PropertyAssessmentData,
                               protodata: List[ProtoData],
                               metadata: Metadata,
                               compartments: Map[String, String],
                               items: List[SurveyLevelItem]
                             )
object PropertyAssessment {
  implicit val reads: Reads[PropertyAssessment] = Json.reads[PropertyAssessment]
  implicit val writes: OWrites[PropertyAssessment] = OWrites { data =>
    Json.obj(
      "id" -> data.id,
      "idx" -> data.idx,
      "name" -> data.name.map(JsString.apply).getOrElse(JsNull),
      "label" -> data.label,
      "description" -> data.description.map(JsString.apply).getOrElse(JsNull),
      "origination" -> data.origination,
      "termination" -> data.termination.map(JsString.apply).getOrElse(JsNull),
      "category" -> data.category,
      "type" -> data.`type`,
      "class" -> data.`class`,
      "data" -> data.data,
      "protodata" -> data.protodata,
      "metadata" -> data.metadata,
      "compartments" -> data.compartments,
      "items" -> data.items,
    )
  }
}


// =======================================================
// PropertyData
// =======================================================

case class PropertyData(
                         foreign_ids: List[ForeignId],
                         foreign_names: List[ForeignId],
                         foreign_labels: List[ForeignId],
                         addresses: AddressData,
                         location: LocationData,
                         assessments: List[PropertyAssessment]
                       )
object PropertyData {
  implicit val format: OFormat[PropertyData] = Json.format[PropertyData]
}


// =======================================================
// ROOT: Property
// =======================================================

case class Property(
                     id: Option[Long],
                     idx: Option[String],
                     name: Option[String],
                     label: Option[String],
                     description: Option[String],
                     origination: Option[String],
                     termination: Option[String],
                     category: Option[CodeMeaning],
                     `type`: Option[CodeMeaning],
                     `class`: Option[CodeMeaning],
                     data: Option[PropertyData],
                     protodata: Option[List[ProtoData]],
                     metadata: Option[Metadata],
                     compartments: Option[Map[String, String]],
                     items: Option[List[SurveyLevelItem]]
                   )

object Property {
  implicit val reads: Reads[Property] = Json.reads[Property]
  implicit val writes: OWrites[Property] = OWrites { p =>
    Json.obj(
      "id"           -> p.id.map(JsNumber(_)).getOrElse(JsNull),
      "idx"          -> p.idx,
      "name"         -> p.name,
      "label"        -> p.label,
      "description"  -> p.description,
      "origination"  -> p.origination.map(JsString.apply).getOrElse(JsNull),
      "termination"  -> p.termination.map(JsString.apply).getOrElse(JsNull),
      "category"     -> p.category,
      "type"         -> p.`type`,
      "class"        -> p.`class`,
      "data"         -> p.data,
      "protodata"    -> p.protodata,
      "metadata"     -> p.metadata,
      "compartments" -> p.compartments,
      "items"        -> p.items
    )
  }
}