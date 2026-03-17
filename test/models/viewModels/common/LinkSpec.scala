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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.mvc.Call
import play.api.libs.json.Json

class LinkSpec extends AnyFreeSpec with Matchers {

  "Link" - {

    "must create a valid instance with all fields including visuallyHiddenMessageKey" in {
      val call = Call("GET", "/test-path")

      val link = Link(
        href = call,
        linkId = "testLinkId",
        messageKey = "test.message.key",
        visuallyHiddenMessageKey = Some("hidden.message.key")
      )

      link.href mustBe call
      link.linkId mustBe "testLinkId"
      link.messageKey mustBe "test.message.key"
      link.visuallyHiddenMessageKey mustBe Some("hidden.message.key")
    }

    "must create a valid instance when visuallyHiddenMessageKey is None" in {
      val call = Call("POST", "/submit")

      val link = Link(
        href = call,
        linkId = "submitLink",
        messageKey = "submit.key",
        visuallyHiddenMessageKey = None
      )

      link.href mustBe call
      link.linkId mustBe "submitLink"
      link.messageKey mustBe "submit.key"
      link.visuallyHiddenMessageKey mustBe None
    }

    "must allow visuallyHiddenMessageKey to use default None" in {
      val call = Call("GET", "/default")

      val link = Link(
        href = call,
        linkId = "defaultLink",
        messageKey = "default.message.key"
      )

      link.visuallyHiddenMessageKey mustBe None
    }
  }
}
