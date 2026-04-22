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

package models.bridge.relationhship

import models.bridge.common.*
import play.api.libs.json.{JsNull, JsNumber, JsString, Json, OFormat, OWrites, Reads}

case class RelationshipManifestation(
                                      artifact_reference: Option[String],
                                      artifact_code: Option[String],
                                      artifact_description: Option[String],
                                      issued_date: Option[String],
                                      withdrawn_date: Option[String],
                                      effective_from_date: Option[String],
                                      effective_to_date: Option[String],
                                      observed_date: Option[String],
                                      operative_area_code: Option[String],
                                      operative_area_name: Option[String],
                                      protodata_ptr: Option[String],
                                      notes: Option[String]
                                    )

object RelationshipManifestation {
  implicit val reads: Reads[RelationshipManifestation] = Json.reads[RelationshipManifestation]

  implicit val writes: OWrites[RelationshipManifestation] = OWrites { data =>
    Json.obj(
      "artifact_reference"    -> data.artifact_reference.map(JsString.apply).getOrElse(JsNull),
      "artifact_code"         -> data.artifact_code.map(JsString.apply).getOrElse(JsNull),
      "artifact_description"  -> data.artifact_description.map(JsString.apply).getOrElse(JsNull),
      "issued_date"           -> data.issued_date.map(JsString.apply).getOrElse(JsNull),
      "withdrawn_date"        -> data.withdrawn_date.map(JsString.apply).getOrElse(JsNull),
      "effective_from_date"   -> data.effective_from_date.map(JsString.apply).getOrElse(JsNull),
      "effective_to_date"     -> data.effective_to_date.map(JsString.apply).getOrElse(JsNull),
      "observed_date"         -> data.observed_date.map(JsString.apply).getOrElse(JsNull),
      "operative_area_code"   -> data.operative_area_code.map(JsString.apply).getOrElse(JsNull),
      "operative_area_name"   -> data.operative_area_name.map(JsString.apply).getOrElse(JsNull),
      "protodata_ptr"         -> data.protodata_ptr.map(JsString.apply).getOrElse(JsNull),
      "notes"                 -> data.notes.map(JsString.apply).getOrElse(JsNull)
    )
  }
}

case class RelationshipData(
                             foreign_ids: List[ForeignId],
                             foreign_names: List[ForeignId],
                             foreign_labels: List[ForeignId],
                             manifestations: List[RelationshipManifestation]
                           )

object RelationshipData {
  implicit val format: OFormat[RelationshipData] =
    Json.format[RelationshipData]
}

case class RelationshipItemTransportation(
                                           path: String
                                         )

object RelationshipItemTransportation {
  implicit val format: OFormat[RelationshipItemTransportation] =
    Json.format[RelationshipItemTransportation]
}

case class RelationshipItemPersistence(
                                        place: String,
                                        identifier: Option[String]
                                      )

object RelationshipItemPersistence {

  implicit val reads: Reads[RelationshipItemPersistence] = Json.reads[RelationshipItemPersistence]

  implicit val writes: OWrites[RelationshipItemPersistence] = OWrites { data =>
    Json.obj(
      "place" -> data.place,
      "identifier" -> data.identifier.map(JsString.apply).getOrElse(JsNull)
    )
  }
}

case class RelationshipItem(
                             transportation: RelationshipItemTransportation,
                             persistence: RelationshipItemPersistence
                           )

object RelationshipItem {
  implicit val format: OFormat[RelationshipItem] =
    Json.format[RelationshipItem]
}

case class Relationship(
                         id: Option[Long],
                         idx: String,
                         name: String,
                         label: String,
                         description: String,
                         origination: Option[String],
                         termination: Option[String],
                         category: CodeMeaning,
                         `type`: CodeMeaning,
                         `class`: CodeMeaning,
                         data: RelationshipData,
                         protodata: List[ProtoData],
                         metadata: Metadata,
                         compartments: Map[String, String],
                         items: List[RelationshipItem]
                       )

object Relationship {
  implicit val reads: Reads[Relationship] = Json.reads[Relationship]
  implicit val writes: OWrites[Relationship] = OWrites { data =>
    Json.obj(
      "id" -> data.id.map(JsNumber(_)).getOrElse(JsNull),
      "idx" -> data.idx,
      "name" -> data.name,
      "label" -> data.label,
      "description" -> data.description,
      "origination" -> data.origination.map(JsString.apply).getOrElse(JsNull),
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