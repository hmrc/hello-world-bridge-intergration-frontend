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

package models.properties

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*
import models.bridge.relationhship.*
import models.bridge.property.*
import models.bridge.person.*
import models.bridge.common.*


class PropertiesForAssessmentResponseSpec extends AnyWordSpec with Matchers {

  "PropertiesForAssessmentResponse" should {
    "serialise and deserialise correctly with nested structures" in {
      
      val proto = ProtoData("application/json", "Test", is_pointer = false, "", "{}")

      val stage = MetadataStage(selecting = Map("k" -> "v"))

      val metadata = Metadata(
        SendingMetadata(stage, stage, stage),
        ReceivingMetadata(stage, stage, stage)
      )

      val codeMeaning = CodeMeaning(Some("C"), Some("Meaning"))

      // PersonItemData and Person sample
      val personData = PersonItemData(
        foreign_ids = List(ForeignId("SYS", "LOC", "1")),
        foreign_names = Nil,
        foreign_labels = Nil,
        names = NameData(Some("Mr"), None, Some("John"), Some("Doe"), None, None, None, None),
        communications = Communications(Some("A Street"), None, Some("john@example.com"))
      )

      val person = Person(
        id = Some(100),
        idx = "P1",
        name = "John Doe",
        label = "Label",
        description = "A person",
        origination = Some("2020"),
        termination = None,
        category = codeMeaning,
        `type` = codeMeaning,
        `class` = codeMeaning,
        data = personData,
        protodata = List(proto),
        metadata = metadata,
        compartments = Map("zone" -> "safe"),
        items = Nil
      )

      // Property and PropertyData sample
      val propertyData = PropertyData(
        foreign_ids = List(ForeignId("SYS", "P", "11")),
        foreign_names = Nil,
        foreign_labels = Nil,
        addresses = AddressData(Some("123 Road"), None, Some("Z1 1ZZ"), None),
        location = LocationData(Some("AUTH"), None, None),
        assessments = Nil
      )

      val property = Property(
        id = Some(200),
        idx = Some("PR1"),
        name = Some("Property Name"),
        label = Some("Label"),
        description = Some("A property"),
        origination = Some("2021"),
        termination = None,
        category = Some(codeMeaning),
        `type` = Some(codeMeaning),
        `class` = Some(codeMeaning),
        data = Some(propertyData),
        protodata = Some(List(proto)),
        metadata = Some(metadata),
        compartments = Some(Map("meta" -> "value")),
        items = None
      )

      // Relationship sample
      val manifestation = RelationshipManifestation(
        artifact_reference = Some("REF"),
        artifact_code = Some("CODE"),
        artifact_description = None,
        issued_date = None,
        withdrawn_date = None,
        effective_from_date = None,
        effective_to_date = None,
        observed_date = None,
        operative_area_code = None,
        operative_area_name = None,
        protodata_ptr = None,
        notes = Some("Note")
      )

      val relationshipData = RelationshipData(
        foreign_ids = List(ForeignId("SYS", "X", "5")),
        foreign_names = Nil,
        foreign_labels = Nil,
        manifestations = List(manifestation)
      )

      val relationshipItem = RelationshipItem(
        transportation = RelationshipItemTransportation("/org/rel"),
        persistence = RelationshipItemPersistence("STORE", Some(123))
      )

      val relationship = Relationship(
        id = Some(300),
        idx = "R1",
        name = "Rel Name",
        label = "Label",
        description = "Relationship",
        origination = Some("2019"),
        termination = None,
        category = codeMeaning,
        `type` = codeMeaning,
        `class` = codeMeaning,
        data = relationshipData,
        protodata = List(proto),
        metadata = metadata,
        compartments = Map("comp" -> "val"),
        items = List(relationshipItem)
      )
      
      val response = PropertiesForAssessmentResponse(
        properties = List(property),
        persons = List(person),
        relationships = List(relationship)
      )
      
      val json = Json.toJson(response)

      json.as[PropertiesForAssessmentResponse] shouldBe response
    }
  }
}

