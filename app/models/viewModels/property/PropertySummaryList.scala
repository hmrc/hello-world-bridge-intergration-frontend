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

package models.viewModels.property
import models.bridge.common.ForeignId
import models.bridge.person.Person
import models.bridge.property.Property
import models.bridge.relationhship.Relationship
import models.viewModels.common.BridgeSummaryListRow
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

trait PropertySummaryList {

  def createPersonSummaryList(person: List[Person])(implicit messages: Messages): SummaryList = {
    val propertyRows: Seq[BridgeSummaryListRow] =
      person.flatMap { person =>
        Seq(
          Some(
            BridgeSummaryListRow(
              messages("person id:"),
              None,
              Seq(person.id.toString),
              changeLink = None
            )
          ),

          Some(
            BridgeSummaryListRow(
              messages("person idx:"),
              None,
              Seq(person.idx),
              changeLink = None
            )
          ),

          Some(
            BridgeSummaryListRow(
              messages("person name:"),
              None,
              Seq(person.name),
              changeLink = None
            )
          ),

          Some(
            BridgeSummaryListRow(
              messages("person label:"),
              None,
              Seq(person.label),
              changeLink = None
            )
          ),

          Some(
            BridgeSummaryListRow(
              messages("person description:"),
              None,
              Seq(person.description),
              changeLink = None
            )
          ),

          Some(
            BridgeSummaryListRow(
              messages("person origination:"),
              None,
              Seq(person.origination.getOrElse("")),
              changeLink = None
            )
          ),

          person.termination.map { termination =>
            BridgeSummaryListRow(
              messages("person termination:"),
              None,
              Seq(termination),
              changeLink = None
            )
          },


          person.category.code.map { code =>
            BridgeSummaryListRow(
              messages("person category.code:"),
              None,
              Seq(code),
              changeLink = None
            )
          },

          // Only show if category.meaning exists
          person.category.meaning.map { meaning =>
            BridgeSummaryListRow(
              messages("person category.meaning:"),
              None,
              Seq(meaning),
              changeLink = None
            )
          },

          person.`type`.code.map { code =>
            BridgeSummaryListRow(
              messages("person type.code:"),
              None,
              Seq(code),
              changeLink = None
            )
          },

          person.`type`.meaning.map { meaning =>
            BridgeSummaryListRow(
              messages("person type.meaning:"),
              None,
              Seq(meaning),
              changeLink = None
            )
          },

          person.`class`.code.map { code =>
            BridgeSummaryListRow(
              messages("person class.code:"),
              None,
              Seq(code),
              changeLink = None
            )
          },

          person.`class`.meaning.map { meaning =>
            BridgeSummaryListRow(
              messages("person class.meaning:"),
              None,
              Seq(meaning),
              changeLink = None
            )
          },

          person.data.names.crown_name.map { crown_name =>
            BridgeSummaryListRow(
              messages("person.data.names.crown_name:"),
              None,
              Seq(crown_name),
              changeLink = None
            )
          },

          person.data.names.corporate_name.map { corporate_name =>
            BridgeSummaryListRow(
              messages("person.data.names.corporate_name:"),
              None,
              Seq(corporate_name),
              changeLink = None
            )
          },

          person.data.names.surname.map { surname =>
            BridgeSummaryListRow(
              messages("person.data.names.surname:"),
              None,
              Seq(surname),
              changeLink = None
            )
          },

          person.data.names.forenames.map { forenames =>
            BridgeSummaryListRow(
              messages("person.data.names.forenames:"),
              None,
              Seq(forenames),
              changeLink = None
            )
          },

          person.data.names.known_as.map { known_as =>
            BridgeSummaryListRow(
              messages("person.data.names.known_as:"),
              None,
              Seq(known_as),
              changeLink = None
            )
          },

          person.data.names.post_nominals.map { post_nominals =>
            BridgeSummaryListRow(
              messages("person.data.names.post_nominals:"),
              None,
              Seq(post_nominals),
              changeLink = None
            )
          },

          person.data.names.title_common.map { title_common =>
            BridgeSummaryListRow(
              messages("person.data.names.title_common:"),
              None,
              Seq(title_common),
              changeLink = None
            )
          },

          person.data.names.title_uncommon.map { title_uncommon =>
            BridgeSummaryListRow(
              messages("person.data.names.title_uncommon:"),
              None,
              Seq(title_uncommon),
              changeLink = None
            )
          },

          person.data.foreign_ids.map { foreign_ids =>
            BridgeSummaryListRow(
              messages("person.data.foreign_ids.value:"),
              None,
              Seq(foreign_ids.value),
              changeLink = None
            )
          },

          person.data.foreign_ids.map { foreign_ids =>
            BridgeSummaryListRow(
              messages("person.data.foreign_ids.location:"),
              None,
              Seq(foreign_ids.location),
              changeLink = None
            )
          },

          person.data.foreign_ids.map { foreign_ids =>
            BridgeSummaryListRow(
              messages("person.data.foreign_ids.system:"),
              None,
              Seq(foreign_ids.system),
              changeLink = None
            )
          },

          person.data.communications.email.map { email =>
            BridgeSummaryListRow(
              messages("person.data.communications.email:"),
              None,
              Seq(email),
              changeLink = None
            )
          },

          person.data.communications.postal_address.map { postal_address =>
            BridgeSummaryListRow(
              messages("person.data.communications.postal_address"),
              None,
              Seq(postal_address),
              changeLink = None
            )
          },

          person.data.communications.telephone_number.map { telephone_number =>
            BridgeSummaryListRow(
              messages("person.data.communications.telephone_numberr:"),
              None,
              Seq(telephone_number),
              changeLink = None
            )
          },

          person.data.foreign_names.map { foreign_names =>
            BridgeSummaryListRow(
              messages("person.data.foreign_names.system:"),
              None,
              Seq(foreign_names.system),
              changeLink = None
            )
          },

          person.data.foreign_names.map { foreign_names =>
            BridgeSummaryListRow(
              messages("person.data.foreign_names.value:"),
              None,
              Seq(foreign_names.value),
              changeLink = None
            )
          },

          person.data.foreign_names.map { foreign_names =>
            BridgeSummaryListRow(
              messages("person.data.foreign_names.location:"),
              None,
              Seq(foreign_names.location),
              changeLink = None
            )
          },

          person.data.foreign_labels.map { foreign_labels =>
            BridgeSummaryListRow(
              messages("person.data.foreign_labels.value:"),
              None,
              Seq(foreign_labels.value),
              changeLink = None
            )
          },

          person.data.foreign_labels.map { foreign_labels =>
            BridgeSummaryListRow(
              messages("person.data.foreign_labels.location:"),
              None,
              Seq(foreign_labels.location),
              changeLink = None
            )
          },

          person.data.foreign_labels.map { foreign_labels =>
            BridgeSummaryListRow(
              messages("person.data.foreign_labels.system:"),
              None,
              Seq(foreign_labels.system),
              changeLink = None
            )
          },
        ).flatten
      }
    SummaryList(propertyRows.map(BridgeSummaryListRow.summarise))
  }
  
  def createPropertySummaryList(
                                 properties: List[Property]
                               )(implicit messages: Messages): SummaryList = {

    def row(key: String, value: String): BridgeSummaryListRow =
      BridgeSummaryListRow(
        messages(key),
        None,
        Seq(value),
        changeLink = None
      )

    def optRow(key: String, opt: Option[String]): Option[BridgeSummaryListRow] =
      opt.map(v => row(key, v))

    def longOptRow(key: String, opt: Option[Long]): Option[BridgeSummaryListRow] =
      opt.map(v => row(key, v.toString))

    def foreignIdRows(prefix: String, ids: List[ForeignId]): Seq[BridgeSummaryListRow] =
      ids.flatMap { id =>
        Seq(
          row(s"$prefix.system:", id.system),
          row(s"$prefix.location:", id.location),
          row(s"$prefix.value:", id.value)
        )
      }

    val propertyRows: Seq[BridgeSummaryListRow] =
      properties.flatMap { property =>

        // -------------------------
        // Top-level fields
        // -------------------------

        val baseRows =
          Seq(
            property.id.map(id => row("property id:", id.toString)),
            property.idx.map(idx => row("property idx:", idx)),
            property.name.map(name => row("property name:", name)),
            property.label.map(label => row("property label:", label)),
            property.description.map(desc => row("property description:", desc)),
            property.origination.map(o => row("property origination:", o)),
            property.termination.map(t => row("property termination:", t)),

            property.category.flatMap(_.code)
              .map(c => row("property category.code:", c)),
            property.category.flatMap(_.meaning)
              .map(m => row("property category.meaning:", m)),

            property.`type`.flatMap(_.code)
              .map(c => row("property type.code:", c)),
            property.`type`.flatMap(_.meaning)
              .map(m => row("property type.meaning:", m)),

            property.`class`.flatMap(_.code)
              .map(c => row("property class.code:", c)),
            property.`class`.flatMap(_.meaning)
              .map(m => row("property class.meaning:", m))
          ).collect { case Some(r) => r }

        // -------------------------
        // PropertyData
        // -------------------------

        val dataRows =
          property.data.toList.flatMap { data =>
            Seq(
              data.location.google_maps
                .map(v => row("property data.location.google_maps:", v)),
              data.location.ordnance_survey
                .map(v => row("property data.location.ordnance_survey:", v)),
              data.location.local_authority_pseudo_area_code
                .map(v => row("property data.location.local_authority_pseudo_area_code:", v)),

              data.addresses.property_full_address
                .map(v => row("property data.address.full:", v)),
              
              data.addresses.address_line_1
                .map(v => row("property data.address.line1:", v)),
              data.addresses.address_postcode
                .map(v => row("property data.address.postcode:", v)),
              data.addresses.known_as
                .map(v => row("property data.address.known_as:", v)),

              Option.when(data.assessments.nonEmpty) {
                row("property data.assessments.count:", data.assessments.size.toString)
              }
            ).collect { case Some(r) => r }
          }

        // -------------------------
        // Assessments
        // -------------------------

        val assessmentRows =
          property.data.toList.flatMap { data =>
            data.assessments.flatMap { assessment =>

              val baseAssessmentRows =
                Seq(
                  Some(row("assessment.id:", assessment.id.toString)),
                  Some(row("assessment.idx:", assessment.idx)),
                  optRow("assessment.name:", assessment.name),
                  Some(row("assessment.label:", assessment.label)),
                  optRow("assessment.description:", assessment.description),
                  Some(row("assessment.origination:", assessment.origination)),
                  optRow("assessment.termination:", assessment.termination),

                  assessment.category.code
                    .map(c => row("assessment.category.code:", c)),
                  assessment.category.meaning
                    .map(m => row("assessment.category.meaning:", m)),

                  assessment.`type`.code
                    .map(c => row("assessment.type.code:", c)),
                  assessment.`type`.meaning
                    .map(m => row("assessment.type.meaning:", m)),

                  assessment.`class`.code
                    .map(c => row("assessment.class.code:", c)),
                  assessment.`class`.meaning
                    .map(m => row("assessment.class.meaning:", m))
                ).collect { case Some(r) => r }

              val foreignRows =
                foreignIdRows("assessment.data.foreign_ids", assessment.data.foreign_ids) ++
                  foreignIdRows("assessment.data.foreign_names", assessment.data.foreign_names) ++
                  foreignIdRows("assessment.data.foreign_labels", assessment.data.foreign_labels)

              val propertyRefRows =
                Seq(
                  row(
                    "assessment.data.property.property_id:",
                    assessment.data.property.property_id.toString
                  ),
                  row(
                    "assessment.data.property.cdb_property_id:",
                    assessment.data.property.cdb_property_id.toString
                  )
                )

              val valuationRows =
                Seq(
                  optRow(
                    "assessment.data.valuation.valuation_method_code:",
                    assessment.data.valuation.valuation_method_code
                  ),
                  longOptRow(
                    "assessment.data.valuation.valuation_rateable:",
                    assessment.data.valuation.valuation_rateable
                  ),
                  optRow(
                    "assessment.data.valuation.valuation_effective_date:",
                    assessment.data.valuation.valuation_effective_date
                  )
                ).collect { case Some(r) => r }

              val listRows =
                Seq(
                  optRow(
                    "assessment.data.list.list_category:",
                    assessment.data.list.list_category
                  ),
                  optRow(
                    "assessment.data.list.list_function:",
                    assessment.data.list.list_function
                  ),
                  optRow(
                    "assessment.data.list.list_year:",
                    assessment.data.list.list_year
                  ),
                  optRow(
                    "assessment.data.list.list_authority_code:",
                    assessment.data.list.list_authority_code
                  )
                ).collect { case Some(r) => r }

              val workflowRows =
                assessment.data.workflow.cdb_job_id
                  .map(id =>
                    row(
                      "assessment.data.workflow.cdb_job_id:",
                      id.toString
                    )
                  )
                  .toSeq

              val useRows =
                Seq(
                  optRow(
                    "assessment.data.use.is_composite:",
                    assessment.data.use.is_composite
                  ),
                  optRow(
                    "assessment.data.use.is_part_exempt:",
                    assessment.data.use.is_part_exempt
                  ),
                  optRow(
                    "assessment.data.use.use_description:",
                    assessment.data.use.use_description
                  )
                ).collect { case Some(r) => r }

              baseAssessmentRows ++
                foreignRows ++
                propertyRefRows ++
                valuationRows ++
                listRows ++
                workflowRows ++
                useRows
            }
          }

        // -------------------------
        // Other data
        // -------------------------

        val compartmentRows =
          property.compartments
            .getOrElse(Map.empty)
            .map { case (k, v) =>
              row("property compartments:", s"$k: $v")
            }
            .toSeq

        val protoRows =
          property.protodata
            .getOrElse(Nil)
            .map(p => row("property protodata:", p.toString))

        val itemRows =
          property.items
            .getOrElse(Nil)
            .map(i => row("property items:", i.idx))

        baseRows ++
          dataRows ++
          assessmentRows ++
          compartmentRows ++
          protoRows ++
          itemRows
      }

    SummaryList(propertyRows.map(BridgeSummaryListRow.summarise))
  }
}
