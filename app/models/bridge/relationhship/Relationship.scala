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

import models.bridge.common._
import play.api.libs.json.{Json, OFormat}

case class Manifestation(
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

object Manifestation {
  implicit val format: OFormat[Manifestation] = Json.format[Manifestation]
}

case class RelationshipItem(
                             transportation: Transportation,
                             persistence: Persistence
                           )

object RelationshipItem {
  implicit val format: OFormat[RelationshipItem] = Json.format[RelationshipItem]
}

case class Transportation(
                           path: String
                         )

object Transportation {
  implicit val format: OFormat[Transportation] = Json.format[Transportation]
}

case class Persistence(
                        place: String,
                        identifier: String
                      )

object Persistence {
  implicit val format: OFormat[Persistence] = Json.format[Persistence]
}

case class RelationshipData(
                             foreign_ids: List[ForeignId],
                             foreign_names: List[ForeignId],
                             foreign_labels: List[ForeignId],
                             manifestations: List[Manifestation]
                           )

object RelationshipData {
  implicit val format: OFormat[RelationshipData] = Json.format[RelationshipData]
}


case class Relationship(
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
                         data: RelationshipData,
                         protodata: List[ProtoData],
                         metadata: Metadata,
                         compartments: Map[String, String],
                         items: List[RelationshipItem]
                       )

object Relationship {
  implicit val format: OFormat[Relationship] = Json.format[Relationship]
}
