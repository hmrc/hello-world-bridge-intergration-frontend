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

package forms

import org.scalatest.matchers.should.Matchers
import models.registration.Postcode
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers.*
import play.api.data.Form
import play.api.libs.json.{Json, JsSuccess}
import org.scalatest.matchers.must.Matchers.mustBe


class FindAPropertyFormSpec extends AnyWordSpec with Matchers {


  "FindAPropertyForm.toString" should {
    "include the property name and postcode when property name is defined" in {
      val postcode = Postcode("AB1 2CD")
      val form = FindAPropertyForm(
        postcode = postcode,
        propertyName = Some("My House")
      )
      form.toString shouldBe "Some(My House),AB1 2CD"
    }

    "include None and postcode when property name is not defined" in {
      val postcode = Postcode("AB1 2CD")
      val form = FindAPropertyForm(
        postcode = postcode,
        propertyName = None
      )
      form.toString shouldBe "None,AB1 2CD"
    }
  }
}
