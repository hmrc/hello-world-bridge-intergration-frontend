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

package service

import models.bridge.relationhship.*
import models.bridge.property.*
import models.{CheckMode, UserAnswers}
import play.api.i18n.Messages
import play.api.mvc.Call
import models.checkAnswers.*
import models.checkAnswers.CheckAnswersSummaryListRow.summarise
import pages.property.*
import controllers.routes
import pages.relationship.*
import pages.{ContactNumberPage, UserNamePage}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

object CheckAnswers {

  // -----------------------------------------------------
  // Base row builder
  // -----------------------------------------------------

  def buildRow(
                labelKey: String,
                value: String,
                linkId: String,
                href: Option[Call],
                hiddenKey: String
              )(implicit messages: Messages): CheckAnswersSummaryListRow =
    CheckAnswersSummaryListRow(
      titleMessageKey = labelKey,
      captionKey = None,
      value = Seq(value),
      changeLink = href.map { call =>
        Link(
          href = call,
          linkId = linkId,
          messageKey = "site.change",
          visuallyHiddenMessageKey = Some(hiddenKey)
        )
      }
    )

  // -----------------------------------------------------
  // Conditional helpers
  // -----------------------------------------------------

  private def requiredRow(
                           labelKey: String,
                           value: String,
                           href: Option[Call] = None
                         )(implicit messages: Messages): CheckAnswersSummaryListRow =
    buildRow(labelKey = labelKey, value = value, linkId = labelKey, href = href, hiddenKey = labelKey)

  private def optionalRow(
                           labelKey: String,
                           value: Option[String],
                           href: Option[Call] = None
                         )(implicit messages: Messages): Option[CheckAnswersSummaryListRow] =
    value.filter(_.nonEmpty).map { v =>
      buildRow(labelKey = labelKey, value = v, linkId = labelKey, href = href, hiddenKey = labelKey)
    }

  private def listRow(
                       labelKey: String,
                       values: List[String]
                     )(implicit messages: Messages): Option[CheckAnswersSummaryListRow] =
    Option.when(values.nonEmpty) {
      buildRow(labelKey, values.mkString(", "), labelKey, None, labelKey)
    }

  // -----------------------------------------------------
  // Rich manifestation rendering
  // -----------------------------------------------------

  private def manifestationRows(
                                 manifestation: RelationshipManifestation,
                                 index: Int
                               )(implicit messages: Messages): Seq[CheckAnswersSummaryListRow] = {

    def opt(labelSuffix: String, value: Option[String]) =
      value.filter(_.nonEmpty).map { v =>
        buildRow(
          labelKey  = s"checkAnswers.manifestation.$labelSuffix",
          value     = v,
          linkId    = s"manifestation-$index-$labelSuffix",
          href      = None,
          hiddenKey = s"manifestation-$index-$labelSuffix"
        )
      }

    Seq(
      opt("artifactReference", manifestation.artifact_reference),
      opt("artifactCode", manifestation.artifact_code),
      opt("artifactDescription", manifestation.artifact_description),
      opt("issuedDate", manifestation.issued_date),
      opt("withdrawnDate", manifestation.withdrawn_date),
      opt("effectiveFrom", manifestation.effective_from_date),
      opt("effectiveTo", manifestation.effective_to_date),
      opt("observedDate", manifestation.observed_date),
      opt(
        "operativeArea",
        for {
          code <- manifestation.operative_area_code
          name <- manifestation.operative_area_name
        } yield s"$code – $name"
      ),
      opt("protodataPtr", manifestation.protodata_ptr),
      opt("notes", manifestation.notes)
    ).flatten
  }

  private def manifestationsSummaryRows(
                                         manifestations: List[RelationshipManifestation]
                                       )(implicit messages: Messages): Seq[CheckAnswersSummaryListRow] =
    manifestations.zipWithIndex.flatMap {
      case (m, idx) => manifestationRows(m, idx + 1)
    }

  // -----------------------------------------------------
  // Relationship summary
  // -----------------------------------------------------

  def createRatePayersPropertyLinksSummaryRows(
                                                answers: UserAnswers
                                              )(implicit messages: Messages): SummaryList = {

    val rows: Seq[Option[CheckAnswersSummaryListRow]] = Seq(

      answers.get(RelationshipIdPage)
        .map(id => requiredRow("checkAnswers.relationship.id", id.toString)),

      answers.get(RelationshipIdxPage)
        .map(idx => requiredRow("checkAnswers.relationship.idx", idx)),

      answers.get(RelationshipNamePage)
        .map(name => requiredRow("checkAnswers.relationship.name", name)),

      answers.get(RelationshipLabelPage)
        .map(label => requiredRow(
          "checkAnswers.relationship.label",
          label,
          href = Some(routes. RelationshipLabelController.onPageLoad()))),

      answers.get(RelationshipDescriptionPage)
        .map(description => requiredRow(
          "checkAnswers.relationship.description",
          description,
          href = Some(routes. RelationshipDescriptionController.onPageLoad())
        )),

      answers.get(RelationshipOriginationPage)
        .map(origination => requiredRow("checkAnswers.relationship.origination", origination)),

      answers.get(RelationshipTerminationPage)
        .map(termination => requiredRow("checkAnswers.relationship.termination", termination)),

      answers.get(RelationshipCategoryCodePage)
        .map(categoryCode => requiredRow("checkAnswers.relationship.categoryCode", categoryCode)),

      answers.get(RelationshipCategoryMeaningPage)
        .map(categoryMeaning => requiredRow("checkAnswers.relationship.categoryMeaning", categoryMeaning)),

      answers.get(RelationshipTypeCodePage)
        .map(typeCode => requiredRow("checkAnswers.relationship.typeCode", typeCode)),

      answers.get(RelationshipTypeMeaningPage)
        .map(typeMeaning => requiredRow("checkAnswers.relationship.typeMeaning", typeMeaning)),

      answers.get(RelationshipClassCodePage)
        .map(classCode => requiredRow("checkAnswers.relationship.classCode", classCode)),

      answers.get(RelationshipClassMeaningPage)
        .map(classMeaning => requiredRow("checkAnswers.relationship.classMeaning", classMeaning)),

      answers.get(PropertyAddressPage)
        .map(address => requiredRow("checkAnswers.property.data.address.full", address)),

      answers.get(PropertyPostcodePage)
        .map(postcode => requiredRow("checkAnswers.property.data.address.postcode", postcode)),
    )
    SummaryList(
      rows.flatten.map(summarise),
      classes = "govuk-!-margin-bottom-9"
    )
  }

  // -----------------------------------------------------
  // ✅ Property summary (ADDED)
  // -----------------------------------------------------

  def createPropertySummaryRows(
                                 answers: UserAnswers
                               )(implicit messages: Messages): SummaryList = {

    val rows: Seq[Option[CheckAnswersSummaryListRow]] = Seq(

      answers.get(PropertyIdPage)
        .map(id => requiredRow("checkAnswers.property.id", id.toString)),

      answers.get(PropertyIdxPage)
        .map(idx => requiredRow("checkAnswers.property.idx", idx)),

      answers.get(PropertyLabelPage)
        .map(label => requiredRow("checkAnswers.property.label", label, href = Some(routes.PropertyLabelController.onPageLoad()))),
      
      answers.get(PropertyOriginationPage)
        .map(orig => requiredRow("checkAnswers.property.origination", orig)),

      optionalRow(
        "checkAnswers.property.name",
        answers.get(PropertyNamePage)
      ),

      optionalRow(
        "checkAnswers.property.description",
        answers.get(PropertyDescriptionPage)
      ),

      optionalRow(
        "checkAnswers.property.termination",
        answers.get(PropertyTerminationPage)
      ),

      optionalRow(
        "checkAnswers.property.category.code",
        answers.get(PropertyCategoryCodePage)
      ),

      optionalRow(
        "checkAnswers.property.data.address.full",
        answers.get(PropertyAddressPage),
        href = Some(routes.PropertyAddressController.onPageLoad())
      ),

      optionalRow(
        "checkAnswers.property.data.address.postcode",
        answers.get(PropertyPostcodePage),
        href = Some(routes.PropertyLabelController.onPageLoad())
      ),

      optionalRow(
        "checkAnswers.property.data.property.value",
        answers.get(PropertyValuePage),
        href = Some(routes.PropertyLabelController.onPageLoad())
      ),

      optionalRow(
        "checkAnswers.property.category.meaning",
        answers.get(PropertyCategoryMeaningPage)
      ),

      // ------------------------------------------------
      // Type
      // ------------------------------------------------
      optionalRow(
        "checkAnswers.property.type.code",
        answers.get(PropertyTypeCodePage)
      ),

      optionalRow(
        "checkAnswers.property.type.meaning",
        answers.get(PropertyTypeMeaningPage)
      ),

      // ------------------------------------------------
      // Class
      // ------------------------------------------------
      optionalRow(
        "checkAnswers.property.class.code",
        answers.get(PropertyClassCodePage)
      ),

      optionalRow(
        "checkAnswers.property.class.meaning",
        answers.get(PropertyClassMeaningPage)
      ),

      // ------------------------------------------------
      // Items count
      // ------------------------------------------------
      answers.get(PropertyItemsCountPage).map { count =>
        buildRow(
          labelKey = "checkAnswers.property.items.count",
          value = count.toString,
          linkId = "checkAnswers.property.items.count",
          href = None,
          hiddenKey = "checkAnswers.property.items.count"
        )
      }
    )

    SummaryList(
      rows.flatten.map(summarise),
      classes = "govuk-!-margin-bottom-9"
    )
  }

// -----------------------------------------------------
  // Registration summary
  // -----------------------------------------------------

  def createRegistrationSummaryRows(
                                     answers: UserAnswers
                                   )(implicit messages: Messages): SummaryList = {

    val rows = Seq(
      answers.get(UserNamePage).map { userName =>
        buildRow(
          labelKey = "checkAnswers.userName",
          value = userName,
          linkId = "user-name",
          href = Some(controllers.routes.UserNameController.onPageLoad(CheckMode)),
          hiddenKey = "user-name"
        )
      },
      answers.get(ContactNumberPage).map { contactNumber =>
        buildRow(
          labelKey = "checkAnswers.contactNumber",
          value = contactNumber.toString,
          linkId = "contact-number",
          href = Some(controllers.routes.ContactNumberController.onPageLoad(CheckMode)),
          hiddenKey = "contact-number"
        )
      }
    )

    SummaryList(
      rows.flatten.map(summarise),
      classes = "govuk-!-margin-bottom-9"
    )
  }
}