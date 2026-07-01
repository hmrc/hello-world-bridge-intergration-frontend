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

import models.registration.Postcode
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.Form
import play.api.libs.json.{JsSuccess, Json}

class FindAPropertyBridgeFormSpec extends AnyWordSpec with Matchers {

  "FindAPropertyForm.toString" should {
    "include the property name and postcode when property name is defined" in {
      val postcode = Postcode("AB1 2CD")
      val form = FindAPropertyBridgeForm(
        postcode = postcode,
        propertyName = Some("My House")
      )

      form.toString shouldBe "Some(My House),AB1 2CD"
    }

    "include None and postcode when property name is not defined" in {
      val postcode = Postcode("AB1 2CD")
      val form = FindAPropertyBridgeForm(
        postcode = postcode,
        propertyName = None
      )

      form.toString shouldBe "None,AB1 2CD"
    }
  }

  "FindAPropertyForm JSON format" should {
    "serialize to JSON" in {
      val form = FindAPropertyBridgeForm(
        postcode = Postcode("AB1 2CD"),
        propertyName = Some("My House")
      )

      Json.toJson(form) shouldBe Json.obj(
        "postcode" -> Json.obj("value" -> "AB1 2CD"),
        "propertyName" -> "My House"
      )
    }

    "deserialize from JSON" in {
      val json = Json.obj(
        "postcode" -> Json.obj("value" -> "AB1 2CD"),
        "propertyName" -> "My House"
      )
      Json.fromJson[FindAPropertyBridgeForm](json) shouldBe JsSuccess(FindAPropertyBridgeForm(Postcode("AB1 2CD"), Some("My House")))
    }
  }

  "FindAPropertyBridgeForm.unapply" should {
    "return postcode and property name" in {
      val form = FindAPropertyBridgeForm(
        postcode = Postcode("AB1 2CD"),
        propertyName = Some("My House")
      )

      FindAPropertyBridgeForm.unapply(form) shouldBe Some((Postcode("AB1 2CD"), Some("My House")))
    }
  }

  "FindAPropertyBridgeForm.form" should {
    "bind valid data successfully" in {
      val data = Map(
        "postcode-value" -> "AB1 2CD",
        "property-name-value" -> "My House"
      )

      val boundForm = FindAPropertyBridgeForm.form.bind(data)
      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(FindAPropertyBridgeForm(Postcode("AB1 2CD"), Some("My House")))
    }

    "strip whitespace from postcode before binding" in {
      val data = Map(
        "postcode-value" -> "  AB1 2CD  ",
        "property-name-value" -> "My House"
      )

      val boundForm = FindAPropertyBridgeForm.form.bind(data)
      boundForm.value.map(_.postcode.value) shouldBe Some("AB1 2CD")
    }

    "error when postcode is empty" in {
      val data = Map(
        "postcode-value" -> "",
        "property-name-value" -> "My House"
      )

      val boundForm = FindAPropertyBridgeForm.form.bind(data)
      boundForm.hasErrors shouldBe true
      boundForm.errors.map(_.message) should contain("findAProperty.postcode.empty.error")
    }

    "error when postcode is invalid" in {
      val data = Map(
        "postcode-value" -> "INVALID",
        "property-name-value" -> "My House"
      )

      val boundForm = FindAPropertyBridgeForm.form.bind(data)
      boundForm.hasErrors shouldBe true
      boundForm.errors.map(_.message) should contain("findAProperty.postcode.invalid.error")
    }

    "error when property name exceeds max length" in {
      val data = Map(
        "postcode-value" -> "AB1 2CD",
        "property-name-value" -> "a" * 101
      )

      val boundForm = FindAPropertyBridgeForm.form.bind(data)
      boundForm.hasErrors shouldBe true
      boundForm.errors.map(_.message) should contain("findAProperty.property.invalid.error")
    }

    "allow property name to be empty" in {
      val data = Map(
        "postcode-value" -> "AB1 2CD"
      )

      val boundForm = FindAPropertyBridgeForm.form.bind(data)
      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(FindAPropertyBridgeForm(Postcode("AB1 2CD"), None))
    }
  }
}