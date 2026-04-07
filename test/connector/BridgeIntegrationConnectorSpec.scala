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

package connector

import connectors.BridgeIntegrationConnector
import mocks.MockHttpV2
import models.bridge.common.{CodeMeaning, ForeignId, Metadata, MetadataStage, ProtoData, ReceivingMetadata, SendingMetadata}
import models.bridge.person.{Communications, NameData, Person, PersonItem, PersonItemData, Persons}
import models.bridge.property.*
import models.bridge.relationhship.{Manifestation, Persistence, RelationshipData, RelationshipItem, Transportation}
import models.dashboard.RatepayerStatusResponse
import models.properties.RatepayerPropertyLinksResponse
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api
import play.api.http.Status
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.client.HttpClientV2


class BridgeIntegrationConnectorSpec extends MockHttpV2
  with GuiceOneAppPerSuite {

  override lazy val app = new GuiceApplicationBuilder()
    .overrides(
      api.inject.bind[HttpClientV2].toInstance(mockHttpClientV2)
    )
    .configure("bridgeIntegration" -> "http://localhost:1300")
    .build()

  val connector: BridgeIntegrationConnector = app.injector.instanceOf[BridgeIntegrationConnector]

  "BridgeIntegrationConnector.registerRatePayer" should {

    "return true when OK is returned" in {
      setupMockHttpV2Post(
        "http://localhost:1300/bridge-integration/register-ratepayer/123456789567"
      )(
        HttpResponse(OK, Json.obj(), Map.empty)
      )

      connector.registerRatePayer(testRegistrationModel).futureValue mustBe true
    }

    "return false when NOT_ACCEPTABLE (406) is returned" in {
      setupMockHttpV2Post(
        "http://localhost:1300/bridge-integration/register-ratepayer/123456789567"
      )(
        HttpResponse(NOT_ACCEPTABLE, Json.obj(), Map.empty)
      )

      connector.registerRatePayer(testRegistrationModel).futureValue mustBe false
    }

    "return false when INTERNAL_SERVER_ERROR (500) is returned" in {
      setupMockHttpV2Post(
        "http://localhost:1300/bridge-integration/register-ratepayer/123456789567"
      )(
        HttpResponse(INTERNAL_SERVER_ERROR, Json.obj(), Map.empty)
      )

      connector.registerRatePayer(testRegistrationModel).futureValue mustBe false
    }

    "return false when an exception is thrown" in {
      setupMockHttpV2FailedPost(
        "http://localhost:1300/bridge-integration/register-ratepayer/123456789567"
      )

      connector.registerRatePayer(testRegistrationModel).futureValue mustBe false
    }
  }

  "BridgeIntegrationConnector.getDashboard" should {

    "return Some(response) when OK (200) is returned" in {
      val json =
        Json.parse(
          """
            |{
            |  "activeRatepayerPersonExists": true,
            |  "activeRatepayerPersonaExists": false,
            |  "activePropertyLinkCount": 7
            |}
          """.stripMargin
        )

      setupMockHttpV2Get(
        "http://localhost:1300/bridge-integration/dashboard/123456789567"
      )(
        Some(RatepayerStatusResponse(true, false, 7))
      )

      val result = connector.getDashboard().futureValue
      result.value.activeRatepayerPersonExists mustBe true
      result.value.activeRatepayerPersonaExists mustBe false
      result.value.activePropertyLinkCount mustBe 7
    }

    "return None when NOT_FOUND (404) is returned" in {
      setupMockHttpV2Get(
        "http://localhost:1300/bridge-integration/dashboard/123456789567"
      )(
        None
      )

      connector.getDashboard().futureValue mustBe None
    }

    "return None when an exception is thrown" in {
      setupMockFailedHttpV2Get(
        "http://localhost:1300/bridge-integration/dashboard/123456789567"
      )

      val result = connector.getDashboard().futureValue
      result mustBe None
    }
  }

  "BridgeIntegrationConnector.getProperties" should {

    "return Some(response) with empty lists when OK (200) is returned" in {
      val json =
        Json.parse(
          """{
            |"properties":[
            |{"id":5,"idx":"1.1.2","name":"testperson2","label":"Individual","description":"Test Person2 Description","origination":"20000101T000000Z","category":{"code":"LTX-DOM-PRP","meaning":"Local taxation domain property"},"type":{"code":"OCC","meaning":"Constituted by reference to actual occupation"},"class":{"code":"HDT","meaning":"Statutory NDR hereditament"},"data":{"foreign_ids":[{"system":"CDB","location":"UK","value":"123456789234"}],"foreign_names":[],"foreign_labels":[],"addresses":{},"location":{},"assessments":[{"id":15,"idx":"1.9.1","name":"Emilya","label":"Individual","description":"User account for HR system","origination":"20250827T000000Z","category":{"code":"LTX-DOM-AST","meaning":"Local taxation domain assessment"},"type":{"code":"CHG","meaning":"To be determined"},"class":{"code":"RLE","meaning":"Register list entry"},"data":{"foreign_ids":[{"system":"HMRC-VOA_CDB","location":"hmrc/voa/cdb/ndr_assessments","value":"27399699000"}],"foreign_names":[],"foreign_labels":[],"property":{"property_id":5,"cdb_property_id":5},"use":{},"valuation_surveys":[],"valuations":[],"valuation":{},"list":{},"workflow":{}},"protodata":[{"mime_type":"img","label":"label","is_pointer":true,"pointer":"","data":""}],"metadata":{"sending":{"extracting":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"loading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}},"receiving":{"unloading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"storing":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}}},"compartments":{},"items":[]}]},"protodata":[],"metadata":{"sending":{"extracting":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"loading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}},"receiving":{"unloading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"storing":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}}},"compartments":{},"items":[]}
            |],
            |"persons":[
            |{"id":34,"idx":"1.6.1","name":"John","label":"Active","description":"User account for HR system","origination":"20250101T000000Z","category":{"code":"","meaning":""},"type":{"code":"INF","meaning":"Assisting information"},"class":{"code":"RPN","meaning":"Register ratepayer"},"data":{"foreign_ids":[{"system":"Government_Gateway","location":"UK","value":"123456789234"}],"foreign_names":[{"system":"SystemX","location":"UK","value":"123456789234"}],"foreign_labels":[{"system":"SystemX","location":"UJ","value":"123456789234"}],"names":{},"communications":{"postal_address":"5 simmonds view, Bristol","telephone_number":"+44 786 453 1243","email":"person7@example.com"}},"protodata":[{"mime_type":"mime","label":"lbl","is_pointer":true,"pointer":"pointer","data":"data"}],"metadata":{"sending":{"extracting":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"loading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}},"receiving":{"unloading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"storing":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}}},"compartments":{},"items":[{"id":23,"idx":"1.5.1","name":"Persona5","label":"Individual","description":"Test Desc","origination":"20250101T000000Z","category":{"code":"LTX-DOM-PSA","meaning":"Local taxation domain persona"},"type":{"code":"TXP","meaning":"LGFA taxpayer"},"class":{"code":"RPO","meaning":"Ratepayer (occupier)"},"data":{"foreign_ids":[{"system":"PSNAFID","location":"UK","value":"brin@exam.com"}],"foreign_names":[],"foreign_labels":[],"names":{},"communications":{}},"protodata":[],"metadata":{"sending":{"extracting":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"loading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}},"receiving":{"unloading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"storing":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}}},"compartments":{},"items":[]}]}
            |],
            |"relationships":[]
            |}""".stripMargin
        )

      setupMockHttpV2Get(
        "http://localhost:1300/bridge-integration/properties"
      )(
        Some(RatepayerPropertyLinksResponse(
          List(
            Property(
              id = 5,
              idx = "1.1.2",
              name = "testperson2",
              label = "Individual",
              description = "Test Person2 Description",
              origination = "20000101T000000Z",
              termination = None,
              category = CodeMeaning(Some("LTX-DOM-PRP"),Some("Local taxation domain property")),
              CodeMeaning(Some("OCC"),Some("Constituted by reference to actual occupation")),
              CodeMeaning(Some("HDT"),Some("Statutory NDR hereditament")),
              PropertyData(
                List(ForeignId("CDB","UK","123456789234")),
                List(),
                List(),
                AddressData(None,None,None,None),
                LocationData(None,None,None),
                List(
                  PropertyAssessment(
                    15,
                    "1.9.1",
                    "Emilya",
                    "Individual",
                    "User account for HR system",
                    "20250827T000000Z",
                    None,
                    CodeMeaning(Some("LTX-DOM-AST"),Some("Local taxation domain assessment")),
                    CodeMeaning(Some("CHG"),Some("To be determined")),
                    CodeMeaning(Some("RLE"),Some("Register list entry")),
                    PropertyAssessmentData(
                      List(ForeignId("HMRC-VOA_CDB","hmrc/voa/cdb/ndr_assessments","27399699000")),
                      List(),
                      List(),
                      PropertyReference(5,5),
                      PropertyUse(None,None,None),
                      List(),
                      List(),
                      ValuationData(None,None,None),
                      ListData(None,None,None,None),
                      WorkflowData(None)
                    ),
                    List(ProtoData("img","label",true,"","")),
                    Metadata(
                      SendingMetadata(
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
                      ReceivingMetadata(
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
                        MetadataStage(Map(),
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
                        ),
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
                          Map())
                      )
                    ),
                    Map(),
                    List()
                  )
                )
              ),
              List(),
              Metadata(
                SendingMetadata(
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
                  ),
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
                  ),
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
                ReceivingMetadata(
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
                  ),
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
                  ),
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
              Map(),
              List()
            )
          ),
          List(
            Person(
              Some(34),
              "1.6.1",
              "John",
              "Active",
              "User account for HR system",
              Some("20250101T000000Z"),
              None,
              CodeMeaning(
                Some(""),
                Some("")
              ),
              CodeMeaning(
                Some("INF"),
                Some("Assisting information")
              ),
              CodeMeaning(
                Some("RPN"),
                Some("Register ratepayer")
              ),
              PersonItemData(
                List(
                  ForeignId(
                    "Government_Gateway",
                    "UK",
                    "123456789234"
                  )
                ),
                List(
                  ForeignId(
                    "SystemX",
                    "UK",
                    "123456789234"
                  )
                ),
                List(
                  ForeignId(
                    "SystemX",
                    "UJ",
                    "123456789234"
                  )
                ),
                NameData(
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None
                ),
                Communications(
                  Some("5 simmonds view, Bristol"),
                  Some("+44 786 453 1243"),
                  Some("person7@example.com"))
              ),
              List(
                ProtoData(
                  "mime",
                  "lbl",
                  true,
                  "pointer",
                  "data")
              ),
              Metadata(
                SendingMetadata(
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
                  ),
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
                  ),
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
                ReceivingMetadata(
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
                  ),
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
                  ),
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
              Map(),
              List(
                PersonItem(
                  Some(23),
                  "1.5.1",
                  "Persona5",
                  "Individual",
                  "Test Desc",
                  Some("20250101T000000Z"),
                  None,
                  CodeMeaning(Some("LTX-DOM-PSA"),Some("Local taxation domain persona")),
                  CodeMeaning(Some("TXP"),Some("LGFA taxpayer")),
                  CodeMeaning(Some("RPO"),Some("Ratepayer (occupier)")),
                  PersonItemData(
                    List(
                      ForeignId(
                        "PSNAFID",
                        "UK",
                        "brin@exam.com")
                    ),
                    List(),
                    List(),
                    NameData(
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None
                    ),
                    Communications(
                      None,
                      None,
                      None)
                  ),
                  List(),
                  Metadata(
                    SendingMetadata(
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
                      ),
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
                      ),
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
                    ReceivingMetadata(
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
                      ),
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
                      ),
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
                  Map(),
                  List()
                )
              )
            )
          ),
          List()
        )
        )
      )

      val result = connector.getProperties.futureValue
      result.map(_.properties.isEmpty) mustBe Some(false)
      result.map(_.persons.isEmpty) mustBe Some(false)
      result.map(_.relationships.isEmpty) mustBe Some(true)
    }

    "return None when NOT_FOUND (404) is returned" in {
      setupMockHttpV2Get(
        "http://localhost:1300/bridge-integration/properties"
      )(
        None
      )
      connector.getProperties.futureValue mustEqual None
    }

    "return None when an exception is thrown" in {
      setupMockFailedHttpV2Get(
        "http://localhost:1300/bridge-integration/properties"
      )

      val result = connector.getProperties().futureValue
      result mustBe None
    }
  }

  "BridgeIntegrationConnector.getRatepayerProperties" should {

    "return Some(response) with empty lists when OK (200) is returned" in {
      val json =
        Json.parse(
          """{
            |"properties":[
            |{"id":5,"idx":"1.1.2","name":"testperson2","label":"Individual","description":"Test Person2 Description","origination":"20000101T000000Z","category":{"code":"LTX-DOM-PRP","meaning":"Local taxation domain property"},"type":{"code":"OCC","meaning":"Constituted by reference to actual occupation"},"class":{"code":"HDT","meaning":"Statutory NDR hereditament"},"data":{"foreign_ids":[{"system":"CDB","location":"UK","value":"123456789234"}],"foreign_names":[],"foreign_labels":[],"addresses":{},"location":{},"assessments":[{"id":15,"idx":"1.9.1","name":"Emilya","label":"Individual","description":"User account for HR system","origination":"20250827T000000Z","category":{"code":"LTX-DOM-AST","meaning":"Local taxation domain assessment"},"type":{"code":"CHG","meaning":"To be determined"},"class":{"code":"RLE","meaning":"Register list entry"},"data":{"foreign_ids":[{"system":"HMRC-VOA_CDB","location":"hmrc/voa/cdb/ndr_assessments","value":"27399699000"}],"foreign_names":[],"foreign_labels":[],"property":{"property_id":5,"cdb_property_id":5},"use":{},"valuation_surveys":[],"valuations":[],"valuation":{},"list":{},"workflow":{}},"protodata":[{"mime_type":"img","label":"label","is_pointer":true,"pointer":"","data":""}],"metadata":{"sending":{"extracting":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"loading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}},"receiving":{"unloading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"storing":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}}},"compartments":{},"items":[]}]},"protodata":[],"metadata":{"sending":{"extracting":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"loading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}},"receiving":{"unloading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"storing":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}}},"compartments":{},"items":[]}
            |],
            |"persons":[
            |{"id":34,"idx":"1.6.1","name":"John","label":"Active","description":"User account for HR system","origination":"20250101T000000Z","category":{"code":"","meaning":""},"type":{"code":"INF","meaning":"Assisting information"},"class":{"code":"RPN","meaning":"Register ratepayer"},"data":{"foreign_ids":[{"system":"Government_Gateway","location":"UK","value":"123456789234"}],"foreign_names":[{"system":"SystemX","location":"UK","value":"123456789234"}],"foreign_labels":[{"system":"SystemX","location":"UJ","value":"123456789234"}],"names":{},"communications":{"postal_address":"5 simmonds view, Bristol","telephone_number":"+44 786 453 1243","email":"person7@example.com"}},"protodata":[{"mime_type":"mime","label":"lbl","is_pointer":true,"pointer":"pointer","data":"data"}],"metadata":{"sending":{"extracting":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"loading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}},"receiving":{"unloading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"storing":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}}},"compartments":{},"items":[{"id":23,"idx":"1.5.1","name":"Persona5","label":"Individual","description":"Test Desc","origination":"20250101T000000Z","category":{"code":"LTX-DOM-PSA","meaning":"Local taxation domain persona"},"type":{"code":"TXP","meaning":"LGFA taxpayer"},"class":{"code":"RPO","meaning":"Ratepayer (occupier)"},"data":{"foreign_ids":[{"system":"PSNAFID","location":"UK","value":"brin@exam.com"}],"foreign_names":[],"foreign_labels":[],"names":{},"communications":{}},"protodata":[],"metadata":{"sending":{"extracting":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"loading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}},"receiving":{"unloading":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"transforming":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}},"storing":{"selecting":{},"filtering":{},"supplementing":{},"recontextualising":{},"readying":{},"assuring":{},"signing":{},"encrypting":{},"sending":{},"receiving":{},"decrypting":{},"verifying":{},"dropping":{},"restoring":{},"inserting":{}}}},"compartments":{},"items":[]}]}
            |],
            |"relationships":[]
            |}""".stripMargin
        )

      setupMockHttpV2Get(
        "http://localhost:1300/bridge-integration/ratepayer-properties/123456789567"
      )(
        Some(RatepayerPropertyLinksResponse(
          List(
            Property(
              id = 5,
              idx = "1.1.2",
              name = "testperson2",
              label = "Individual",
              description = "Test Person2 Description",
              origination = "20000101T000000Z",
              termination = None,
              category = CodeMeaning(Some("LTX-DOM-PRP"), Some("Local taxation domain property")),
              CodeMeaning(Some("OCC"), Some("Constituted by reference to actual occupation")),
              CodeMeaning(Some("HDT"), Some("Statutory NDR hereditament")),
              PropertyData(
                List(ForeignId("CDB", "UK", "123456789567")),
                List(),
                List(),
                AddressData(None, None, None, None),
                LocationData(None, None, None),
                List(
                  PropertyAssessment(
                    15,
                    "1.9.1",
                    "Emilya",
                    "Individual",
                    "User account for HR system",
                    "20250827T000000Z",
                    None,
                    CodeMeaning(Some("LTX-DOM-AST"), Some("Local taxation domain assessment")),
                    CodeMeaning(Some("CHG"), Some("To be determined")),
                    CodeMeaning(Some("RLE"), Some("Register list entry")),
                    PropertyAssessmentData(
                      List(ForeignId("HMRC-VOA_CDB", "hmrc/voa/cdb/ndr_assessments", "27399699000")),
                      List(),
                      List(),
                      PropertyReference(5, 5),
                      PropertyUse(None, None, None),
                      List(),
                      List(),
                      ValuationData(None, None, None),
                      ListData(None, None, None, None),
                      WorkflowData(None)
                    ),
                    List(ProtoData("img", "label", true, "", "")),
                    Metadata(
                      SendingMetadata(
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
                      ReceivingMetadata(
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
                        MetadataStage(Map(),
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
                        ),
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
                          Map())
                      )
                    ),
                    Map(),
                    List()
                  )
                )
              ),
              List(),
              Metadata(
                SendingMetadata(
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
                  ),
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
                  ),
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
                ReceivingMetadata(
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
                  ),
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
                  ),
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
              Map(),
              List()
            )
          ),
          List(
            Person(
              Some(34),
              "1.6.1",
              "John",
              "Active",
              "User account for HR system",
              Some("20250101T000000Z"),
              None,
              CodeMeaning(
                Some(""),
                Some("")
              ),
              CodeMeaning(
                Some("INF"),
                Some("Assisting information")
              ),
              CodeMeaning(
                Some("RPN"),
                Some("Register ratepayer")
              ),
              PersonItemData(
                List(
                  ForeignId(
                    "Government_Gateway",
                    "UK",
                    "123456789234"
                  )
                ),
                List(
                  ForeignId(
                    "SystemX",
                    "UK",
                    "123456789234"
                  )
                ),
                List(
                  ForeignId(
                    "SystemX",
                    "UJ",
                    "123456789234"
                  )
                ),
                NameData(
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None,
                  None
                ),
                Communications(
                  Some("5 simmonds view, Bristol"),
                  Some("+44 786 453 1243"),
                  Some("person7@example.com"))
              ),
              List(
                ProtoData(
                  "mime",
                  "lbl",
                  true,
                  "pointer",
                  "data")
              ),
              Metadata(
                SendingMetadata(
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
                  ),
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
                  ),
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
                ReceivingMetadata(
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
                  ),
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
                  ),
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
              Map(),
              List(
                PersonItem(
                  Some(23),
                  "1.5.1",
                  "Persona5",
                  "Individual",
                  "Test Desc",
                  Some("20250101T000000Z"),
                  None,
                  CodeMeaning(Some("LTX-DOM-PSA"), Some("Local taxation domain persona")),
                  CodeMeaning(Some("TXP"), Some("LGFA taxpayer")),
                  CodeMeaning(Some("RPO"), Some("Ratepayer (occupier)")),
                  PersonItemData(
                    List(
                      ForeignId(
                        "PSNAFID",
                        "UK",
                        "brin@exam.com")
                    ),
                    List(),
                    List(),
                    NameData(
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None,
                      None
                    ),
                    Communications(
                      None,
                      None,
                      None)
                  ),
                  List(),
                  Metadata(
                    SendingMetadata(
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
                      ),
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
                      ),
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
                    ReceivingMetadata(
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
                      ),
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
                      ),
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
                  Map(),
                  List()
                )
              )
            )
          ),
          List()
        )
        )
      )
      val result = connector.getRatepayerProperties()(hc).futureValue
      result.map(_.properties.isEmpty) mustBe Some(false)
      result.map(_.persons.isEmpty) mustBe Some(false)
      result.map(_.relationships.isEmpty) mustBe Some(true)
    }

    "return None when NOT_FOUND (404) is returned" in {
      setupMockHttpV2Get(
        "http://localhost:1300/bridge-integration/ratepayer-properties/123456789567"
      )(
        None
      )
      connector.getRatepayerProperties()(hc).futureValue mustEqual None
    }


    "return None when an exception is thrown" in {
      setupMockFailedHttpV2Get(
        "http://localhost:1300/bridge-integration/ratepayer-properties/123456789567"
      )

      val result = connector.getRatepayerProperties().futureValue
      result mustBe None
    }

  }

  "BridgeIntegrationConnector.exploreRatePayer" should {
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

    val personData = Persons(List(personMax))

    "return Some(response) OK (200) is returned" in {

      setupMockHttpV2Get(
        "http://localhost:1300/bridge-integration/explore-ratepayer/123456789567"
      )(
        Some(personData)
      )

      val result = connector.exploreRatePayer()(hc).futureValue
      result mustBe Some(personData)
    }

    "return None when NOT_FOUND (404) is returned" in {
      setupMockHttpV2Get(
        "http://localhost:1300/bridge-integration/explore-ratepayer/123456789567"
      )(
        None
      )
      connector.exploreRatePayer()(hc).futureValue mustEqual None
    }


    "return None when an exception is thrown" in {
      setupMockFailedHttpV2Get(
        "http://localhost:1300/bridge-integration/explore-ratepayer/123456789567"
      )

      val result = connector.exploreRatePayer()(hc).futureValue
      result mustBe None
    }
  }
}