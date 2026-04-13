/*
 * Copyright 2025 HM Revenue & Customs
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

package models.pages

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pages.Page
import play.api.libs.json.JsPath

class PageSpec extends AnyWordSpec with Matchers {

  "Page implicit toString conversion" should {

    "convert a Page to its string representation implicitly" in {

      object TestPage extends Page {
        override def toString: String = "testPage"
      }

      val pageAsString: String = TestPage

      pageAsString shouldBe "testPage"
    }
  }
}
