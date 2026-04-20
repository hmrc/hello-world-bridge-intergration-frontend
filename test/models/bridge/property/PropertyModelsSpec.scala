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


import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*
import models.bridge.common._

class PropertyModelsSpec extends AnyWordSpec with Matchers {

  "AddressData" should {
    "serialise and deserialise correctly" in {
      val addr = AddressData(
        property_full_address = Some("221B Baker St"),
        address_line_1 = Some("Baker St"),
        address_postcode = Some("NW1 6XE"),
        known_as = Some("Sherlock's Home")
      )

      val json = Json.toJson(addr)
      json.as[AddressData] shouldBe addr
    }

    "support missing optional fields" in {
      val json = Json.parse("""{ "address_postcode": "AB1 2CD" }""")
      val addr = json.as[AddressData]

      addr.address_postcode shouldBe Some("AB1 2CD")
      addr.property_full_address shouldBe None
      addr.known_as shouldBe None
    }
  }

  "LocationData" should {
    "serialise and deserialise correctly" in {
      val loc = LocationData(
        local_authority_pseudo_area_code = Some("E09000033"),
        ordnance_survey = Some("TQ123456"),
        google_maps = Some("gmaps_id")
      )

      val json = Json.toJson(loc)
      json.as[LocationData] shouldBe loc
    }

    "handle empty JSON" in {
      val loc = Json.obj().as[LocationData]
      loc.local_authority_pseudo_area_code shouldBe None
      loc.ordnance_survey shouldBe None
      loc.google_maps shouldBe None
    }
  }

  "PropertyReference" should {
    "round-trip through JSON" in {
      val ref = PropertyReference(1001L, 9999L)
      Json.toJson(ref).as[PropertyReference] shouldBe ref
    }
  }

  "PropertyUse" should {
    "serialise and deserialise correctly" in {
      val use = PropertyUse(Some("true"), Some("false"), Some("Retail"))
      Json.toJson(use).as[PropertyUse] shouldBe use
    }

    "support missing fields" in {
      val json = Json.parse("""{ "use_description": "Industrial" }""")
      val use = json.as[PropertyUse]

      use.use_description shouldBe Some("Industrial")
      use.is_composite shouldBe None
      use.is_part_exempt shouldBe None
    }
  }

  "ValuationData" should {
    "serialise and deserialise correctly" in {
      val vd = ValuationData(
        valuation_method_code = Some("M1"),
        valuation_rateable = Some(150000L),
        valuation_effective_date = Some("2024-04-01")
      )

      Json.toJson(vd).as[ValuationData] shouldBe vd
    }
  }

  "ListData" should {
    "serialise and deserialise correctly" in {
      val ld = ListData(
        list_category = Some("CAT"),
        list_function = Some("FUNC"),
        list_year = Some("2023"),
        list_authority_code = Some("AUTH")
      )

      Json.toJson(ld).as[ListData] shouldBe ld
    }
  }

  "WorkflowData" should {
    "serialise and deserialise correctly" in {
      val wd = WorkflowData(Some(123))
      Json.toJson(wd).as[WorkflowData] shouldBe wd
    }
  }

  "PropertyAssessmentData" should {
    "serialise and deserialise correctly" in {
      val pad = PropertyAssessmentData(
        foreign_ids = List(ForeignId("SYS", "LOC", "123")),
        foreign_names = Nil,
        foreign_labels = Nil,
        property = PropertyReference(1, 2),
        use = PropertyUse(Some("false"), None, Some("Commercial")),
        valuation_surveys = List(Json.obj("survey" -> "A")),
        valuations = List(Json.obj("val" -> 123)),
        valuation = ValuationData(Some("M2"), Some(20000L), Some("2023-04-01")),
        list = ListData(Some("A"), Some("B"), Some("2023"), Some("AUTH")),
        workflow = WorkflowData(Some(999))
      )

      Json.toJson(pad).as[PropertyAssessmentData] shouldBe pad
    }
  }

  "PropertyAssessment" should {
    "serialise and deserialise correctly" in {
      val proto = ProtoData("application/json", "Test", is_pointer = false, "", "{}")
      val stage = MetadataStage(selecting = Map("s" -> "1"))
      val metadata = Metadata(
        SendingMetadata(stage, stage, stage),
        ReceivingMetadata(stage, stage, stage)
      )

      val pad = PropertyAssessmentData(
        foreign_ids = List(ForeignId("SYS", "LON", "7")),
        foreign_names = Nil,
        foreign_labels = Nil,
        property = PropertyReference(10L, 20L),
        use = PropertyUse(Some("true"), Some("false"), Some("Office")),
        valuation_surveys = List(Json.obj("survey" -> "test")),
        valuations = List(Json.obj("value" -> 50000)),
        valuation = ValuationData(Some("M3"), Some(50000L), Some("2021-04-01")),
        list = ListData(Some("CAT"), Some("FUNC"), Some("2021"), Some("AUTH")),
        workflow = WorkflowData(Some(7))
      )

      val assessment = PropertyAssessment(
        id = 1L,
        idx = "IDX1",
        name = Some("Assessment Name"),
        label = "Assessment Label",
        description = Some("Some description"),
        origination = "2020",
        termination = None,
        category = CodeMeaning(Some("CAT"), Some("Category")),
        `type` = CodeMeaning(Some("TYPE"), Some("Type")),
        `class` = CodeMeaning(Some("CLASS"), Some("Class")),
        data = pad,
        protodata = List(proto),
        metadata = metadata,
        compartments = Map("comp" -> "value"),
        items = List.empty
      )

      Json.toJson(assessment).as[PropertyAssessment] shouldBe assessment
    }
  }

  "PropertyData" should {
    "serialise and deserialise nested structure correctly" in {
      val pd = PropertyData(
        foreign_ids = List(ForeignId("SYS", "X", "1")),
        foreign_names = List(ForeignId("SYS2", "Y", "2")),
        foreign_labels = List(ForeignId("SYS3", "Z", "3")),
        addresses = AddressData(Some("Addr"), Some("Line1"), Some("AB1 2CD"), None),
        location = LocationData(Some("AUTH"), Some("OSREF"), Some("GMAPS")),
        assessments = Nil
      )

      Json.toJson(pd).as[PropertyData] shouldBe pd
    }
  }

  "Property" should {
    "serialise and deserialise full nested model correctly" in {
      val proto = ProtoData("application/pdf", "Doc", false, "", "xyz")
      val stage = MetadataStage(selecting = Map("a" -> "b"))
      val metadata = Metadata(
        SendingMetadata(stage, stage, stage),
        ReceivingMetadata(stage, stage, stage)
      )

      val assessment = PropertyAssessment(
        id = 42L,
        idx = "PA",
        name = Some("Assessment"),
        label = "Label",
        description = Some("Desc"),
        origination = "2021",
        termination = Some("2023"),
        category = CodeMeaning(Some("C1"), Some("Cat1")),
        `type` = CodeMeaning(Some("T1"), Some("Type1")),
        `class` = CodeMeaning(Some("CL1"), Some("Class1")),
        data = PropertyAssessmentData(
          foreign_ids = List(ForeignId("SYS", "A", "1")),
          foreign_names = Nil,
          foreign_labels = Nil,
          property = PropertyReference(100, 200),
          use = PropertyUse(None, None, Some("Industrial")),
          valuation_surveys = Nil,
          valuations = Nil,
          valuation = ValuationData(None, None, None),
          list = ListData(None, None, None, None),
          workflow = WorkflowData(None)
        ),
        protodata = List(proto),
        metadata = metadata,
        compartments = Map("zone" -> "restricted"),
        items = List.empty
      )

      val property = Property(
        id = Some(777),
        idx = Some("PROP"),
        name = Some("Main Property"),
        label = Some("Main Label"),
        description = Some("Property description"),
        origination = Some("2020"),
        termination = None,
        category = Some(CodeMeaning(Some("PCAT"), Some("PersonCat"))),
        `type` = Some(CodeMeaning(Some("PT"), Some("Type"))),
        `class` = Some(CodeMeaning(Some("PCL"), Some("Class"))),
        data = Some(PropertyData(
          foreign_ids = List(ForeignId("SYS", "R1", "001")),
          foreign_names = Nil,
          foreign_labels = Nil,
          addresses = AddressData(Some("123 St"), None, Some("ZZ1 1ZZ"), None),
          location = LocationData(None, None, Some("gmaps")),
          assessments = List(assessment)
        )),
        protodata = Some(List(proto)),
        metadata = Some(metadata),
        compartments = Some(Map("meta" -> "data")),
        items = None
      )

      Json.toJson(property).as[Property] shouldBe property
    }
  }
}
