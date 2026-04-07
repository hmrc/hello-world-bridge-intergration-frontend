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

package models.bridge.person

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*
import models.bridge.common._

class PersonModelJsonFormatSpec extends AnyWordSpec with Matchers {

  "NameData" should {
    "serialise and deserialise correctly" in {
      val nd = NameData(
        title_common = Some("Mr"),
        title_uncommon = Some("Dr"),
        forenames = Some("John"),
        surname = Some("Smith"),
        post_nominals = Some("PhD"),
        corporate_name = None,
        crown_name = None,
        known_as = Some("Johnny")
      )

      val json = Json.toJson(nd)
      json.as[NameData] shouldBe nd
    }

    "handle missing optional fields" in {
      val json = Json.parse("""{ "surname": "Doe" }""")
      val nd = json.as[NameData]

      nd.surname shouldBe Some("Doe")
      nd.forenames shouldBe None
      nd.known_as shouldBe None
    }
  }

  "Communications" should {
    "serialise and deserialise correctly" in {
      val comms = Communications(
        postal_address = Some("10 Downing St"),
        telephone_number = Some("0207 123 4567"),
        email = Some("pm@gov.uk")
      )

      val json = Json.toJson(comms)
      json.as[Communications] shouldBe comms
    }

    "support entirely empty JSON (all None)" in {
      val comms = Json.obj().as[Communications]
      comms.postal_address shouldBe None
      comms.telephone_number shouldBe None
      comms.email shouldBe None
    }
  }

  "PersonItemData" should {
    "serialise and deserialise correctly" in {
      val data = PersonItemData(
        foreign_ids = List(ForeignId("SYS", "LOC", "001")),
        foreign_names = List(ForeignId("NAME_SYS", "UK", "A123")),
        foreign_labels = List(ForeignId("LBL_SYS", "EU", "L999")),
        names = NameData(Some("Mr"), None, Some("Clive"), Some("Brown"), None, None, None, None),
        communications = Communications(Some("Address"), None, Some("c.brown@email.com"))
      )

      val json = Json.toJson(data)
      json.as[PersonItemData] shouldBe data
    }
  }

  "PersonItem" should {
    "serialise and deserialise correctly" in {
      val proto = ProtoData("text/plain", "Test", false, "", "data")
      val stage = MetadataStage(selecting = Map("s" -> "1"))
      val metadata = Metadata(
        SendingMetadata(stage, stage, stage),
        ReceivingMetadata(stage, stage, stage)
      )

      val itemData = PersonItemData(
        foreign_ids = List(ForeignId("SYS", "LOC", "001")),
        foreign_names = Nil,
        foreign_labels = Nil,
        names = NameData(Some("Ms"), None, Some("Alice"), Some("Walker"), None, None, None, None),
        communications = Communications(None, None, Some("alice@example.com"))
      )

      val item = PersonItem(
        id = Some(1001L),
        idx = "IDX001",
        name = "Alice Walker",
        label = "PersonItemLabel",
        description = "Test Person Item",
        origination = Some("2020-01-01"),
        termination = None,
        category = CodeMeaning(Some("CAT"), Some("CategoryDesc")),
        `type` = CodeMeaning(Some("TYPE"), Some("TypeDesc")),
        `class` = CodeMeaning(Some("CLASS"), Some("ClassDesc")),
        data = itemData,
        protodata = List(proto),
        metadata = metadata,
        compartments = Map("comp1" -> "val1"),
        items = List(Json.obj("nested" -> "value"))
      )

      val json = Json.toJson(item)

      json.as[PersonItem] shouldBe item
    }
  }

  "Person" should {
    "serialise and deserialise nested data correctly" in {
      val proto = ProtoData("application/bin", "Binary", false, "", "010101")
      val stage = MetadataStage(selecting = Map("x" -> "y"))

      val metadata = Metadata(
        SendingMetadata(stage, stage, stage),
        ReceivingMetadata(stage, stage, stage)
      )

      val data = PersonItemData(
        foreign_ids = List(ForeignId("SYS", "UK", "111")),
        foreign_names = List(ForeignId("SYS2", "UK", "A")),
        foreign_labels = List(ForeignId("SYS3", "UK", "LBL")),
        names = NameData(Some("Sir"), None, Some("Marcus"), Some("Coleman"), None, None, None, None),
        communications = Communications(Some("Addr1"), None, None)
      )

      val childItem = PersonItem(
        id = Some(2000),
        idx = "CHILD",
        name = "Child Item",
        label = "ChildLabel",
        description = "Child Desc",
        origination = Some("2020"),
        termination = None,
        category = CodeMeaning(Some("C1"), Some("Cat1")),
        `type` = CodeMeaning(Some("T1"), Some("Type1")),
        `class` = CodeMeaning(Some("CL1"), Some("Class1")),
        data = data,
        protodata = List(proto),
        metadata = metadata,
        compartments = Map.empty,
        items = Nil
      )

      val person = Person(
        id = Some(9999),
        idx = "IDX-P",
        name = "Marcus Coleman",
        label = "MainPerson",
        description = "A Test Person",
        origination = Some("2021"),
        termination = Some("2025"),
        category = CodeMeaning(Some("PCAT"), Some("PersonCat")),
        `type` = CodeMeaning(Some("PTYPE"), Some("PersonType")),
        `class` = CodeMeaning(Some("PCLASS"), Some("PersonClass")),
        data = data,
        protodata = List(proto),
        metadata = metadata,
        compartments = Map("zone" -> "restricted"),
        items = List(childItem)
      )

      val json = Json.toJson(person)

      json.as[Person] shouldBe person
    }
  }
}
