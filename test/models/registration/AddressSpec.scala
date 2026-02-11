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

package models.registration

import play.api.libs.json.{JsValue, Json}
import helpers.TestSupport
import models.registration.Address

class AddressSpec extends TestSupport {

  val testAddressJsonResponse: JsValue = Json.parse(
    """{"line1":"99","line2":"Wibble Rd", "town":"Worthing", "county":"West Sussex", "postcode":{"value":"BN110AA"}}""".stripMargin)

  "Address" should {
    "deserialize to json" in {
      Json.toJson(testAddress) mustBe testAddressJsonResponse
    }
    "serialize to json" in {
      testAddressJsonResponse.as[Address] mustBe testAddress
    }
    "address .toString method should return an address model as a string" in {
      testAddress.toString mustBe "99, Wibble Rd, Worthing, West Sussex, BN110AA"
    }
  }
}
