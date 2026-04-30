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

case class SourceValue(
                        source: Option[String],
                        value: Option[String]
                      )
object SourceValue {
  implicit val reads: Reads[SourceValue] = Json.reads[SourceValue]

  implicit val writes: OWrites[SourceValue] = OWrites { data =>
    Json.obj(
      "source" -> data.source.map(JsString.apply).getOrElse(JsNull),
      "value" -> data.value.map(JsString.apply).getOrElse(JsNull)
    )
  }
}

case class QuantitySourceValue(
                                source: Option[String],
                                value: Long
                              )
object QuantitySourceValue {
  implicit val reads: Reads[QuantitySourceValue] = Json.reads[QuantitySourceValue]

  implicit val writes: OWrites[QuantitySourceValue] = OWrites { data =>
    Json.obj(
      "source" -> data.source.map(JsString.apply).getOrElse(JsNull),
      "value" -> data.value
    )
  }
}

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

case class SurveyData(
                       artifacts: List[ArtifactRecord],
                       attributions: List[JsObject],
                       constructions: List[JsObject],
                       facilities: List[FacilityRecord],
                       foreign_ids: List[ForeignId],
                       foreign_labels: List[ForeignId],
                       foreign_names: List[ForeignId],
                       uninheritances: List[JsObject],
                       uses: List[JsObject]
                     )
object SurveyData {
  implicit val format: OFormat[SurveyData] = Json.format[SurveyData]
}

case class SurveyLevelItem(
                            id: Long,
                            idx: String,
                            name: Option[String],
                            label: String,
                            description: Option[String],
                            origination: Option[String],
                            termination: Option[String],
                            category: CodeMeaning,
                            `type`: CodeMeaning,
                            `class`: CodeMeaning,
                            data: SurveyData,
                            protodata: List[ProtoData],
                            metadata: Metadata,
                            compartments: Map[String, String],
                            items: List[SurveyLevelItem]
                          )
object SurveyLevelItem {
  implicit val reads: Reads[SurveyLevelItem] = Json.reads[SurveyLevelItem]

  implicit val writes: OWrites[SurveyLevelItem] = OWrites { data =>
    Json.obj(
      "id" -> data.id,
      "idx" -> data.idx,
      "name" -> data.name.map(JsString.apply).getOrElse(JsNull),
      "label" -> data.label,
      "description" -> data.description.map(JsString.apply).getOrElse(JsNull),
      "origination" -> data.origination.map(JsString.apply).getOrElse(JsNull),
      "termination" -> data.termination.map(JsString.apply).getOrElse(JsNull),
      "category" -> data.category,
      "type" -> data.`type` ,
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
// Valuation Survey (KEY FIX)
// =======================================================

case class ValuationSurveyData(
                                foreign_ids: List[ForeignId],
                                foreign_labels: List[ForeignId],
                                foreign_names: List[ForeignId],
                                survey: SurveyLevelItem
                              )
object ValuationSurveyData {
  implicit val format: OFormat[ValuationSurveyData] = Json.format[ValuationSurveyData]
}

case class ValuationSurvey(
                            id: Long,
                            idx: String,
                            name: Option[String],
                            label: String,
                            description: Option[String],
                            origination: Option[String],
                            termination: Option[String],
                            category: CodeMeaning,
                            `type`: CodeMeaning,
                            `class`: CodeMeaning,
                            data: ValuationSurveyData,
                            protodata: List[ProtoData],
                            metadata: Metadata,
                            compartments: Map[String, String],
                            items: List[SurveyLevelItem]
                          )
object ValuationSurvey {
  implicit val reads: Reads[ValuationSurvey] = Json.reads[ValuationSurvey]

  implicit val writes: OWrites[ValuationSurvey] = OWrites { data =>
    Json.obj(
      "id" -> data.id,
      "idx" -> data.idx,
      "name" -> data.name.map(JsString.apply).getOrElse(JsNull),
      "label" -> data.label,
      "description" -> data.description.map(JsString.apply).getOrElse(JsNull),
      "origination" -> data.origination.map(JsString.apply).getOrElse(JsNull),
      "termination" -> data.termination.map(JsString.apply).getOrElse(JsNull),
      "category" -> data.category,
      "type" -> data.`type` ,
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
// Property Assessment
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
  implicit val reads: Reads[PropertyUse] = Json.reads[PropertyUse]

  implicit val writes: OWrites[PropertyUse] = OWrites { data =>
    Json.obj(
      "is_composite" -> data.is_composite.map(JsString.apply).getOrElse(JsNull),
      "is_part_exempt" -> data.is_part_exempt.map(JsString.apply).getOrElse(JsNull),
      "use_description" -> data.use_description.map(JsString.apply).getOrElse(JsNull)
    )
  }
}

case class ValuationData(
                          valuation_method_code: Option[String],
                          valuation_rateable: Option[Long],
                          valuation_effective_date: Option[String]
                        )
object ValuationData {
  implicit val reads: Reads[ValuationData] = Json.reads[ValuationData]

  implicit val writes: OWrites[ValuationData] = OWrites { data =>
    Json.obj(
      "valuation_method_code" -> data.valuation_method_code.map(JsString.apply).getOrElse(JsNull),
      "valuation_rateable" -> data.valuation_rateable.map(JsNumber(_)).getOrElse(JsNull),
      "valuation_effective_date" -> data.valuation_effective_date.map(JsString.apply).getOrElse(JsNull)
    )
  }
}

case class ListData(
                     list_category: Option[String],
                     list_function: Option[String],
                     list_year: Option[String],
                     list_authority_code: Option[String]
                   )
object ListData {
  implicit val reads: Reads[ListData] = Json.reads[ListData]

  implicit val writes: OWrites[ListData] = OWrites { data =>
    Json.obj(
      "list_category" -> data.list_category.map(JsString.apply).getOrElse(JsNull),
      "list_function" -> data.list_function.map(JsString.apply).getOrElse(JsNull),
      "list_year" -> data.list_year.map(JsString.apply).getOrElse(JsNull),
      "list_authority_code" -> data.list_authority_code.map(JsString.apply).getOrElse(JsNull)
    )
  }
}

case class WorkflowData(
                         cdb_job_id: Option[Long]
                       )
object WorkflowData {
  implicit val reads: Reads[WorkflowData] = Json.reads[WorkflowData]

  implicit val writes: OWrites[WorkflowData] = OWrites { data =>
    Json.obj(
      "cdb_job_id" -> data.cdb_job_id.map(JsNumber(_)).getOrElse(JsNull),
    )
  }
}

case class PropertyAssessmentData(
                                   foreign_ids: List[ForeignId],
                                   foreign_names: List[ForeignId],
                                   foreign_labels: List[ForeignId],
                                   property: PropertyReference,
                                   use: PropertyUse,
                                   valuation_surveys: List[ValuationSurvey],
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
                                            assessment: Property
                                          )

object PropertyAssessmentContext {
  implicit val format: OFormat[PropertyAssessmentContext] = Json.format[PropertyAssessmentContext]
}

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
// Property Data
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
// Root Property
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