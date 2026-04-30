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

package models.viewModels.Relationship

import models.bridge.relationhship.Relationship
import models.viewModels.common.BridgeSummaryListRow
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

trait RelationshipSummaryList {
  def createRelationshipSummaryList(person: List[Relationship])(implicit messages: Messages): SummaryList = {
    val propertyRows: Seq[BridgeSummaryListRow] =
      person.flatMap { relationship =>
        Seq(
          Some(
            BridgeSummaryListRow(
              messages("relationship id:"),
              None,
              Seq(relationship.id.toString),
              changeLink = None
            )
          ),

          Some(
            BridgeSummaryListRow(
              messages("relationship idx:"),
              None,
              Seq(relationship.idx),
              changeLink = None
            )
          ),

          Some(
            BridgeSummaryListRow(
              messages("relationship name:"),
              None,
              Seq(relationship.name),
              changeLink = None
            )
          ),

          Some(
            BridgeSummaryListRow(
              messages("relationship label:"),
              None,
              Seq(relationship.label),
              changeLink = None
            )
          ),

          Some(
            BridgeSummaryListRow(
              messages("relationship description:"),
              None,
              Seq(relationship.description),
              changeLink = None
            )
          ),

          Some(
            BridgeSummaryListRow(
              messages("relationship origination:"),
              None,
              Seq(relationship.origination.orNull),
              changeLink = None
            )
          ),

          relationship.termination.map { termination =>
            BridgeSummaryListRow(
              messages("relationship termination:"),
              None,
              Seq(termination),
              changeLink = None
            )
          },


          relationship.category.code.map { code =>
            BridgeSummaryListRow(
              messages("relationship category.code:"),
              None,
              Seq(code),
              changeLink = None
            )
          },

          relationship.category.meaning.map { meaning =>
            BridgeSummaryListRow(
              messages("relationship category.meaning:"),
              None,
              Seq(meaning),
              changeLink = None
            )
          },

          relationship.`type`.code.map { code =>
            BridgeSummaryListRow(
              messages("relationship type.code:"),
              None,
              Seq(code),
              changeLink = None
            )
          },

          relationship.`type`.meaning.map { meaning =>
            BridgeSummaryListRow(
              messages("relationship type.meaning:"),
              None,
              Seq(meaning),
              changeLink = None
            )
          },

          relationship.`class`.code.map { code =>
            BridgeSummaryListRow(
              messages("relationship class.code:"),
              None,
              Seq(code),
              changeLink = None
            )
          },

          relationship.`class`.meaning.map { meaning =>
            BridgeSummaryListRow(
              messages("relationship class.meaning:"),
              None,
              Seq(meaning),
              changeLink = None
            )
          },

          relationship.data.foreign_names.map { foreign_names =>
            BridgeSummaryListRow(
              messages("relationship.data.names.foreign_names.systym:"),
              None,
              Seq(foreign_names.system),
              changeLink = None
            )
          },

          relationship.data.foreign_names.map { foreign_names =>
            BridgeSummaryListRow(
              messages("relationship.data.names.foreign_names.value:"),
              None,
              Seq(foreign_names.value),
              changeLink = None
            )
          },

          relationship.data.foreign_names.map { foreign_names =>
            BridgeSummaryListRow(
              messages("relationship.data.names.foreign_names.location:"),
              None,
              Seq(foreign_names.location),
              changeLink = None
            )
          },


          relationship.data.manifestations.map { m =>
            BridgeSummaryListRow(
              titleMessageKey = messages("relationship.data.manifestations:"),
              captionKey = None,
              value = m.artifact_reference.toSeq,  // converts Option[String] → Seq[String]
              changeLink = None
            )
          },

          relationship.data.foreign_ids.map { foreign_ids =>
            BridgeSummaryListRow(
              messages("person.data.foreign_ids.value:"),
              None,
              Seq(foreign_ids.value),
              changeLink = None
            )
          },

          relationship.data.foreign_ids.map { foreign_ids =>
            BridgeSummaryListRow(
              messages("person.data.foreign_ids.location:"),
              None,
              Seq(foreign_ids.location),
              changeLink = None
            )
          },

          relationship.data.foreign_ids.map { foreign_ids =>
            BridgeSummaryListRow(
              messages("person.data.foreign_ids.system:"),
              None,
              Seq(foreign_ids.system),
              changeLink = None
            )
          },

          relationship.data.foreign_labels.map { foreign_labels =>
            BridgeSummaryListRow(
              messages("relationship.data.foreign_labels.value:"),
              None,
              Seq(foreign_labels.value),
              changeLink = None
            )
          },

          relationship.data.foreign_labels.map { foreign_labels =>
            BridgeSummaryListRow(
              messages("person.data.foreign_labels.location:"),
              None,
              Seq(foreign_labels.location),
              changeLink = None
            )
          },

          relationship.data.foreign_labels.map { foreign_labels =>
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
}
