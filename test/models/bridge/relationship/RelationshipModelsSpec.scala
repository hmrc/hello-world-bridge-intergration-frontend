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

package models.bridge.relationship

import models.bridge.relationhship._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*
import models.bridge.common._

class RelationshipModelsSpec extends AnyWordSpec with Matchers {

  "Manifestation" should {
    "serialise and deserialise correctly" in {
      val m = Manifestation(
        artifact_reference = Some("REF123"),
        artifact_code = Some("CODEX"),
        artifact_description = Some("A test artifact"),
        issued_date = Some("2023-01-01"),
        withdrawn_date = None,
        effective_from_date = Some("2023-03-01"),
        effective_to_date = Some("2024-03-01"),
        observed_date = Some("2023-02-15"),
        operative_area_code = Some("AREA1"),
        operative_area_name = Some("Area Name"),
        protodata_ptr = Some("ptr-xyz"),
        notes = Some("Some notes")
      )

      Json.toJson(m).as[Manifestation] shouldBe m
    }

    "support missing optional fields" in {
      val json = Json.parse("""{ "artifact_code": "A1" }""")
      val m = json.as[Manifestation]

      m.artifact_code shouldBe Some("A1")
      m.artifact_reference shouldBe None
      m.notes shouldBe None
      m.withdrawn_date shouldBe None
    }
  }

  "Transportation" should {
    "serialise and deserialise correctly" in {
      val t = Transportation(path = "/root/child")
      Json.toJson(t).as[Transportation] shouldBe t
    }
  }

  "Persistence" should {
    "serialise and deserialise correctly" in {
      val p = Persistence(place = "DB", identifier = "XYZ123")
      Json.toJson(p).as[Persistence] shouldBe p
    }
  }

  "RelationshipItem" should {
    "serialise and deserialise correctly" in {
      val item = RelationshipItem(
        transportation = Transportation("/path/123"),
        persistence = Persistence("STORE", "ID987")
      )

      Json.toJson(item).as[RelationshipItem] shouldBe item
    }
  }

  "RelationshipData" should {
    "serialise and deserialise correctly" in {
      val data = RelationshipData(
        foreign_ids = List(ForeignId("SYS", "LOC", "1")),
        foreign_names = List(ForeignId("SYS2", "LOC2", "2")),
        foreign_labels = Nil,
        manifestations = List(
          Manifestation(
            artifact_reference = Some("A"),
            artifact_code = Some("B"),
            artifact_description = Some("Desc"),
            issued_date = None,
            withdrawn_date = None,
            effective_from_date = None,
            effective_to_date = None,
            observed_date = None,
            operative_area_code = None,
            operative_area_name = None,
            protodata_ptr = None,
            notes = None
          )
        )
      )

      Json.toJson(data).as[RelationshipData] shouldBe data
    }
  }

  "Relationship" should {
    "serialise and deserialise nested structure correctly" in {
      val proto = ProtoData("application/json", "Proto", false, "", "{}")
      val stage = MetadataStage(selecting = Map("x" -> "y"))
      val metadata = Metadata(
        SendingMetadata(stage, stage, stage),
        ReceivingMetadata(stage, stage, stage)
      )

      val manifestation = Manifestation(
        artifact_reference = Some("R1"),
        artifact_code = Some("C1"),
        artifact_description = None,
        issued_date = Some("2021-01-01"),
        withdrawn_date = None,
        effective_from_date = None,
        effective_to_date = None,
        observed_date = None,
        operative_area_code = None,
        operative_area_name = None,
        protodata_ptr = None,
        notes = Some("note")
      )

      val rdata = RelationshipData(
        foreign_ids = List(ForeignId("SYS", "A", "111")),
        foreign_names = Nil,
        foreign_labels = Nil,
        manifestations = List(manifestation)
      )

      val ritem = RelationshipItem(
        transportation = Transportation("/a/b/c"),
        persistence = Persistence("STORE1", "ID1")
      )

      val relationship = Relationship(
        id = 999,
        idx = "REL1",
        name = "Relationship Name",
        label = "Label",
        description = "A relationship",
        origination = "2020",
        termination = Some("2030"),
        category = CodeMeaning(Some("CAT"), Some("Category")),
        `type` = CodeMeaning(Some("TYPE"), Some("Type")),
        `class` = CodeMeaning(Some("CLASS"), Some("Class")),
        data = rdata,
        protodata = List(proto),
        metadata = metadata,
        compartments = Map("zone" -> "secure"),
        items = List(ritem)
      )

      Json.toJson(relationship).as[Relationship] shouldBe relationship
    }
  }
}
