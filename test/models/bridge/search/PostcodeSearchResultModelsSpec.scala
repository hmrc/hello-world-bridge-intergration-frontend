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

package models.bridge.search

import models.bridge.common.CodeMeaning
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class PostcodeSearchResultSpec extends AnyWordSpec with Matchers {
  
  private val codeMeaning =
    CodeMeaning(
      code = Some("TEST"),
      meaning = Some("Test Meaning")
    )
  
  private val model =
    PostcodeSearchResult(
      Results(
        total_records = 1,
        page_number = 1,
        records = Seq(
          RecordWrapper(
            Record(
              sequence_number = 1,
              data = Data(
                list = ValuationList(
                  id = "LIST1",
                  classification = codeMeaning,
                  country = codeMeaning,
                  collection_authority = codeMeaning,
                  inforcement_period =
                    InforcementPeriod(
                      commencement_date = "2024-01-01",
                      expiration_date = None
                    ),
                  compilation_date = "2024-01-01",
                  valuation_date = "2024-01-01",
                  total_of_all_valuations = Some("1000")
                ),
                list_entry = ListEntry(
                  id = "ENTRY1",
                  designated_person =
                    DesignatedPerson(
                      name = Some("John Smith"),
                      address = Some("1 Test Street"),
                      company_number = Some("123456")
                    ),
                  relevant_property =
                    RelevantProperty(
                      id = "PROP1",
                      full_address = "1 Test Street, Test Town",
                      improvement_ind = Some("Y")
                    ),
                  use =
                    Use(
                      description = Some("Residential"),
                      composite_ind = "N",
                      part_exempt_ind = None
                    ),
                  valuation =
                    Valuation(
                      value = "1000",
                      method = codeMeaning,
                      previous = None
                    ),
                  period =
                    Period(
                      effective_from_date = "2024-01-01",
                      effective_to_date = None
                    ),
                  administration =
                    Administration(
                      alteration_date = None,
                      alteration_seq_no = None,
                      entry_seq_no = None,
                      judicially_ordered_by = None,
                      transitionally_certified = None,
                      collection_authority_ref = None
                    ),
                  workflow =
                    Workflow(
                      creating_job_id = "JOB1"
                    )
                )
              )
            )
          )
        )
      )
    )

  "InforcementPeriod" should {
    "serialise and deserialise correctly" in {
      val period =
        InforcementPeriod(
          commencement_date = "2024-01-01",
          expiration_date = Some("2025-01-01")
        )
      Json.toJson(period).as[InforcementPeriod] mustEqual period
    }
  }
  
  "DesignatedPerson" should {
    "serialise and deserialise correctly" in {
      val person =
        DesignatedPerson(
          name = Some("John Smith"),
          address = Some("1 Test Street"),
          company_number = Some("123456")
        )
        
      Json.toJson(person).as[DesignatedPerson] mustEqual person
    }
  }
  
  "RelevantProperty" should {
    "serialise and deserialise correctly" in {
      val property =
        RelevantProperty(
          id = "PROP1",
          full_address = "1 Test Street",
          improvement_ind = Some("Y")
        )
      Json.toJson(property).as[RelevantProperty] mustEqual property
    }
  }
  
  "Results" should {
    "serialise and deserialise correctly" in {
      Json.toJson(model.results).as[Results] mustEqual model.results
    }
  }

  "RecordWrapper" should {
    "serialise and deserialise correctly" in {
      val wrapper = model.results.records.head
      Json.toJson(wrapper).as[RecordWrapper] mustEqual wrapper
    }
  }

  "PostcodeSearchResult" should {
    "round-trip through JSON" in {
      Json.toJson(model).as[PostcodeSearchResult] mustEqual model
    }
  }
}