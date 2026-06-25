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

import play.api.libs.json.{Json, OFormat}
import models.bridge.common.*

case class PostcodeSearchResult(
                                 results: Results
                               )

object PostcodeSearchResult:
  implicit val format: OFormat[PostcodeSearchResult] = Json.format

case class Results(
                    total_records: Int,
                    page_number: Int,
                    records: Seq[RecordWrapper]
                  )

object Results:
  implicit val format: OFormat[Results] = Json.format

case class RecordWrapper(
                          record: Record
                        )

object RecordWrapper:
  implicit val format: OFormat[RecordWrapper] = Json.format

case class Record(
                   sequence_number: Int,
                   data: Data
                 )

object Record:
  implicit val format: OFormat[Record] = Json.format

case class Data(
                 list: ValuationList,
                 list_entry: ListEntry
               )

object Data:
  implicit val format: OFormat[Data] = Json.format

case class ValuationList(
                          id: String,
                          classification: CodeMeaning,
                          country: CodeMeaning,
                          collection_authority: CodeMeaning,
                          inforcement_period: InforcementPeriod,
                          compilation_date: String,
                          valuation_date: String,
                          total_of_all_valuations: Option[String]
                        )

object ValuationList:
  implicit val format: OFormat[ValuationList] = Json.format

case class InforcementPeriod(
                              commencement_date: String,
                              expiration_date: Option[String]
                            )

object InforcementPeriod:
  implicit val format: OFormat[InforcementPeriod] = Json.format

case class ListEntry(
                      id: String,
                      designated_person: DesignatedPerson,
                      relevant_property: RelevantProperty,
                      use: Use,
                      valuation: Valuation,
                      period: Period,
                      administration: Administration,
                      workflow: Workflow
                    )

object ListEntry:
  implicit val format: OFormat[ListEntry] = Json.format

case class DesignatedPerson(
                             name: Option[String],
                             address: Option[String],
                             company_number: Option[String]
                           )

object DesignatedPerson:
  implicit val format: OFormat[DesignatedPerson] = Json.format

case class RelevantProperty(
                             id: String,
                             full_address: String,
                             improvement_ind: Option[String]
                           )

object RelevantProperty:
  implicit val format: OFormat[RelevantProperty] = Json.format

case class Use(
                description: Option[String],
                composite_ind: String,
                part_exempt_ind: Option[String]
              )

object Use:
  implicit val format: OFormat[Use] = Json.format

case class Valuation(
                      value: String,
                      method: CodeMeaning,
                      previous: Option[String]
                    )

object Valuation:
  implicit val format: OFormat[Valuation] = Json.format

case class Period(
                   effective_from_date: String,
                   effective_to_date: Option[String]
                 )

object Period:
  implicit val format: OFormat[Period] = Json.format

case class Administration(
                           alteration_date: Option[String],
                           alteration_seq_no: Option[Int],
                           entry_seq_no: Option[Int],
                           judicially_ordered_by: Option[String],
                           transitionally_certified: Option[String],
                           collection_authority_ref: Option[String]
                         )

object Administration:
  implicit val format: OFormat[Administration] = Json.format

case class Workflow(
                     creating_job_id: String
                   )

object Workflow:
  implicit val format: OFormat[Workflow] = Json.format