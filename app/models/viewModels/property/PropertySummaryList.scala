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
            println(Console.MAGENTA + code + Console.RESET)
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

  def createPropertySummaryList(property: List[Property])(implicit messages: Messages): SummaryList = {
    val propertyRows: Seq[BridgeSummaryListRow] =

      property.flatMap { property =>
        Seq(
          Some(
            BridgeSummaryListRow(
              messages("property id:"),
              None,
              Seq(property.id.toString),
              changeLink = None
            )
          ),

          Some(
            BridgeSummaryListRow(
              messages("property idx:"),
              None,
              Seq(property.idx),
              changeLink = None
            )
          ),

          Some(
            BridgeSummaryListRow(
              messages("property name:"),
              None,
              Seq(property.name),
              changeLink = None
            )
          ),

          Some(
            BridgeSummaryListRow(
              messages("property label:"),
              None,
              Seq(property.label),
              changeLink = None
            )
          ),

          Some(
            BridgeSummaryListRow(
              messages("property description:"),
              None,
              Seq(property.description),
              changeLink = None
            )
          ),

          Some(
            BridgeSummaryListRow(
              messages("property origination:"),
              None,
              Seq(property.origination),
              changeLink = None
            )
          ),

          property.termination.map { termination =>
            BridgeSummaryListRow(
              messages("property termination:"),
              None,
              Seq(termination),
              changeLink = None
            )
          },

          // Only show if category.code exists
          property.category.code.map { code =>
            BridgeSummaryListRow(
              messages("property category.code:"),
              None,
              Seq(code),
              changeLink = None
            )
          },

          // Only show if category.meaning exists
          property.category.meaning.map { meaning =>
            BridgeSummaryListRow(
              messages("property category.meaning:"),
              None,
              Seq(meaning),
              changeLink = None
            )
          },

          property.`type`.code.map { code =>
          BridgeSummaryListRow(
            messages("property type.code:"),
            None,
            Seq(code),
            changeLink = None
          )
        },

          property.`type`.meaning.map { meaning =>
            BridgeSummaryListRow(
              messages("property type.meaning:"),
              None,
              Seq(meaning),
              changeLink = None
            )
          },

          property.`class`.code.map { code =>
            BridgeSummaryListRow(
              messages("property class.code:"),
              None,
              Seq(code),
              changeLink = None
            )
          },

          property.`class`.meaning.map { meaning =>
            BridgeSummaryListRow(
              messages("property class.meaning:"),
              None,
              Seq(meaning),
              changeLink = None
            )
          },

          property.data.location.google_maps.map { location =>
            BridgeSummaryListRow(
              messages("property.data.location.google_maps:"),
              None,
              Seq(location),
              changeLink = None
            )
          },

          property.data.location.ordnance_survey.map { ordnance =>
            BridgeSummaryListRow(
              messages("property.data.location.ordnance_survey:"),
              None,
              Seq(ordnance),
              changeLink = None
            )
          },

          property.data.location.local_authority_pseudo_area_code.map { local_authority =>
            BridgeSummaryListRow(
              messages("property.data.location.local_authority_pseudo_area_code:"),
              None,
              Seq(local_authority),
              changeLink = None
            )
          },

          property.protodata.map { protoData =>
            BridgeSummaryListRow(
              messages("property.protodata.data:"),
              None,
              Seq(protoData.data),
              changeLink = None
            )
          },

          property.protodata.map { protoData =>
            BridgeSummaryListRow(
              messages("property.protodata.label:"),
              None,
              Seq(protoData.label),
              changeLink = None
            )
          },

          property.protodata.map { protoData =>
            BridgeSummaryListRow(
              messages("property.protodata.pointer:"),
              None,
              Seq(protoData.pointer),
              changeLink = None
            )
          },

          property.protodata.map { protoData =>
            BridgeSummaryListRow(
              messages("property.protodata.mime_type:"),
              None,
              Seq(protoData.mime_type),
              changeLink = None
            )
          },

          property.protodata.map { protoData =>
            BridgeSummaryListRow(
              messages("property.protodata.is_pointer:"),
              None,
              Seq(protoData.is_pointer.toString),
              changeLink = None
            )
          },

          property.items.map { item =>
            BridgeSummaryListRow(
              messages("property.items"),
              None,
              Seq(item.toString),
              changeLink = None
            )
          },

        ).flatten
      }

    SummaryList(propertyRows.map(BridgeSummaryListRow.summarise))
  }

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
              Seq(relationship.origination),
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
            println(Console.MAGENTA + m + Console.RESET)
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