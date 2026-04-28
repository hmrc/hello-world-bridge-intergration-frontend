/*
 * Copyright 2025 HM Revenue & Customs
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

package helpers

import models.registration.RatepayerType.Individual
import models.registration.*
import models.*
import models.bridge.relationhship.*
import models.bridge.property.*
import models.bridge.person.*
import models.bridge.common.*
import play.api.libs.json.{JsValue, Json}

import java.time.{Instant, LocalDate}

trait TestData {

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
  
  val testProperty = Property(
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
    items =  None
  )

  val codeMeaning = CodeMeaning(Some("C"), Some("Meaning"))

  val personData = PersonItemData(
    foreign_ids = List(ForeignId("SYS", "LOC", "1")),
    foreign_names = Nil,
    foreign_labels = Nil,
    names = NameData(Some("Mr"), None, Some("John"), Some("Doe"), None, None, None, None),
    communications = Communications(Some("A Street"), None, Some("john@example.com"))
  )
  
  val testPerson = Person(
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

  val relationshipItem = RelationshipItem(
    transportation = RelationshipItemTransportation("/org/rel"),
    persistence = RelationshipItemPersistence("STORE", Some("ID123"))
  )

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
  
  val testRelationship = Relationship(
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
  
  val testRegistrationModel: RegisterRatepayer =
    RegisterRatepayer(
      userType = Some(Individual),
      agentStatus = Some(AgentStatus.Agent),
      name = Some("John Doe"),
      tradingName = Some(TradingName("CompanyLTD")),
      email = Some("JohnDoe@digital.hmrc.gov.uk"),
      contactNumber = Some("07123456789"),
      secondaryNumber = Some("07123456789"),
      address = Some("99, Wibble Rd, Worthing, West Sussex, BN110AA"),
      trnReferenceNumber = Some(TRNReferenceNumber(ReferenceType.Nino, "12345")),
      isRegistered = Some(false)
    )
  
  val testPostcode: Postcode = Postcode(value = "BH1 7EY")
  val time: Instant = Instant.now()
  val nameModel: Name = Name("Lovely Fella")

  val testAddress: Address =
    Address(
      line1 = "99",
      line2 = Some("Wibble Rd"),
      town = "Worthing",
      county = Some("West Sussex"),
      postcode = Postcode("BN110AA")
    )

  val nameJson: JsValue = Json.parse(
    """
      |{"value":"Lovely Fella"}
      |""".stripMargin)
    
  val contactNumberModel = "0300 200 3310"

  val phoneNumberJson: JsValue = Json.parse(
    """
      |{"value":"0300 200 3310"}
      |""".stripMargin)

  val emailModel: Email = Email("test@digital.hmrc.gov.uk")

  val emailJson: JsValue = Json.parse(
    """
      |{"value":"test@digital.hmrc.gov.uk"}
      |""".stripMargin)

  val ninoModel: Nino = Nino("AA123456A")

  val ninoJson: JsValue = Json.parse(
    """
      |{"nino":"AA123456A"}
      |""".stripMargin)

  val personMax = Person(
    id = Some(1),
    idx = "1",
    name = "John",
    label = "Active",
    description = "User account for HR system",
    origination = Some("2026-02-16T14:40:09Z"),
    termination = Some("2026-02-16T14:40:09Z"),
    category = CodeMeaning(Some("LTX-DOM-JOB"), Some("Local taxation domain job")),
    `type` = CodeMeaning(Some("INF"), Some("")),
    `class` = CodeMeaning(Some("LTX-DOM-JOB"), Some("Local taxation domain job")),
    data = PersonItemData(
      foreign_ids = List(ForeignId(system = "Government_Gateway", location = "UK", value = "123456789234")),
      foreign_names = List(ForeignId(system = "SystemX", location = "UK", value = "123456789234")),
      foreign_labels = List(ForeignId(system = "SystemX", location = "UJ", value = "123456789234")),
      names = NameData(Some("Mr"), None, Some("Clive"), Some("Brown"), None, None, None, None),
      communications = Communications(Some("Address"), None, Some("c.brown@email.com"))
    ),
    protodata = List.empty,
    metadata = Metadata(
      sending = SendingMetadata(
        MetadataStage(
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map()),
        MetadataStage(
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map()),
        MetadataStage(
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map()
        )
      ),
      receiving = ReceivingMetadata(
        MetadataStage(
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map()),
        MetadataStage(
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map()),
        MetadataStage(
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map(),
          Map()
        )
      )
    ),
    compartments = Map.empty,
    items = List.empty)

  val personsDataMax: Persons = Persons(List(personMax))

}
