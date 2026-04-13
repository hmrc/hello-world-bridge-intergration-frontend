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

package models.viewModels.common

import models.components.Link
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Call
import play.api.test.Helpers.stubMessagesApi
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

class BridgeSummaryListRowSpec
  extends AnyWordSpec
    with Matchers
    with OptionValues {
  
  private val messagesApi: MessagesApi =
    stubMessagesApi(
      Map(
        "en" -> Map(
          "test.title"   -> "Test title",
          "test.caption" -> "Test caption",
          "value.one"    -> "Value one",
          "value.two"    -> "Value two",
          "change.text"  -> "Change"
        )
      )
    )
    
  implicit private val messages: Messages =
    messagesApi.preferred(Seq.empty)
  
  "BridgeSummaryListRow.summarise" should {
    "render a key with title and caption when captionKey is defined" in {
      val row = BridgeSummaryListRow(
        titleMessageKey = "test.title",
        captionKey      = Some("test.caption"),
        value           = Seq("value.one"),
        changeLink      = None
      )
      
      val result = BridgeSummaryListRow.summarise(row)
      val html =
        result.key.content
          .asInstanceOf[HtmlContent]
          .value
          .toString
      html should include("Test title")
      html should include("Test caption")
      html should include("govuk-hint")
    }
    
    "render the key as a link when titleLink is defined and no caption is present" in {
      val row = BridgeSummaryListRow(
        titleMessageKey = "test.title",
        captionKey      = None,
        value           = Seq("value.one"),
        changeLink      = None,
        titleLink = Some(
          Link(
            href = Call("GET", "/test-url"),
            messageKey = "test.title",
            visuallyHiddenMessageKey = None,
            linkId = "title-link"
          )
        )
      )
      
      val result = BridgeSummaryListRow.summarise(row)
      val html =
        result.key.content
          .asInstanceOf[HtmlContent]
          .value
          .toString
      html should include("""href="/test-url"""")
      html should include("Test title")
    }
    
    "render a plain text key when neither caption nor titleLink is defined" in {
      val row = BridgeSummaryListRow(
        titleMessageKey = "test.title",
        captionKey      = None,
        value           = Seq("value.one"),
        changeLink      = None
      )
      
      val result = BridgeSummaryListRow.summarise(row)
      result.key.content shouldBe Text("Test title")
    }
    
    "render multiple values joined with <br> and an id derived from the title" in {
      val row = BridgeSummaryListRow(
        titleMessageKey = "Test Title",
        captionKey      = None,
        value           = Seq("value.one", "value.two"),
        changeLink      = None
      )
      
      val result = BridgeSummaryListRow.summarise(row)
      val html =
        result.value.content
          .asInstanceOf[HtmlContent]
          .value
          .toString
      
      html should include("""id="test-title-id"""")
      html should include("Value one")
      html should include("Value two")
    }
    
    "apply valueClasses when provided" in {
      val row = BridgeSummaryListRow(
        titleMessageKey = "test.title",
        captionKey      = None,
        value           = Seq("value.one"),
        changeLink      = None,
        valueClasses    = Some("govuk-!-font-weight-bold")
      )
      
      val result = BridgeSummaryListRow.summarise(row)
      
      val html =
        result.value.content
          .asInstanceOf[HtmlContent]
          .value
          .toString
      html should include("""class="govuk-!-font-weight-bold"""")
    }
    
    "render a change link action when changeLink is provided" in {
      val changeLink = Link(
        href = Call("GET", "/change"),
        messageKey = "change.text",
        visuallyHiddenMessageKey = Some("Hidden"),
        linkId = "change-link"
      )
      
      val row = BridgeSummaryListRow(
        titleMessageKey = "test.title",
        captionKey      = None,
        value           = Seq("value.one"),
        changeLink      = Some(changeLink)
      )
      
      val result = BridgeSummaryListRow.summarise(row)
      val actions = result.actions.value
      actions.items should have size 1

      val item = actions.items.head
      item.href shouldBe "/change"
      item.content shouldBe Text("Change")
      item.attributes("id") shouldBe "change-link"
    }
    
    "render a link in the value column when value is empty and changeLink is defined" in {
      val changeLink = Link(
        href = Call("GET", "/add"),
        messageKey = "change.text",
        visuallyHiddenMessageKey = None,
        linkId = "add-link"
      )
      
      val row = BridgeSummaryListRow(
        titleMessageKey = "test.title",
        captionKey      = None,
        value           = Seq.empty,
        changeLink      = Some(changeLink)
      )
      
      val result = BridgeSummaryListRow.summarise(row)
      val html =
        result.value.content
          .asInstanceOf[HtmlContent]
          .value
          .toString
      
      html should include("""href="/add"""")
      html should include("""id="add-link"""")
      html should include("Change")
    }



    "render an empty value when value and changeLink are both absent" in {
      val row = BridgeSummaryListRow(
        titleMessageKey = "test.title",
        captionKey      = None,
        value           = Seq.empty,
        changeLink      = None
      )
      
      val result = BridgeSummaryListRow.summarise(row)
      result.value shouldBe Value()
    }
  }
}