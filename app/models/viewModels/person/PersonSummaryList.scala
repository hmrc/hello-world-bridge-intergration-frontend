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

package models.viewModels.person

import models.bridge.person.Person
import models.viewModels.common.BridgeSummaryListRow
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

trait PersonSummaryList {

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
              Seq(person.origination),
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
}
