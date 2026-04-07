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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.Helpers.stubMessagesApi
import models.{UserName, UserAnswers}
import pages.ContactNumberPage
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import viewmodels.checkAnswers.contactNumberSummary.row
import org.scalatest.OptionValues.convertOptionToValuable
import models.CheckMode

class ContactNumberSummarySpec extends AnyWordSpec with Matchers {
  
  implicit val messages: Messages = {
    val messagesApi: MessagesApi = stubMessagesApi(
      Map(
        "en" -> Map(
          "contactNumber.change.hidden" -> "Change contact number"
        )
      )
    )
    messagesApi.preferred(Seq.empty)
  }
  
  "row" should {
    "return a SummaryListRow when ContactNumberPage has an answer" in {
      val userAnswers =
        UserAnswers("id")
        .set(ContactNumberPage, "0123456789")
        .success
        .value
      
      val result = row(userAnswers)
      result shouldBe defined
      
      val summaryRow = result.value
      summaryRow.key.content.asHtml.toString should include("contactNumber.checkYourAnswersLabel")
      summaryRow.value.content.asHtml.toString should include("0123456789")

      summaryRow.actions shouldBe defined
      val actionItems = summaryRow.actions.value.items
      actionItems should have size 1
      
      val action = actionItems.head
      action.content.asHtml.toString should include("site.change")
      action.href shouldBe controllers.routes.ContactNumberController.onPageLoad(CheckMode).url
      action.visuallyHiddenText shouldBe Some("Change contact number")
    }
    
    "return None when ContactNumberPage has no answer" in {
      val userAnswers = UserAnswers("id")
      val result = row(userAnswers)
      result shouldBe None
    }
  }
}