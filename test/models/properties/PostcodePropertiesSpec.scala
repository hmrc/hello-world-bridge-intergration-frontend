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

package models.properties

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

import java.time.LocalDate

class PostcodePropertiesSpec extends AnyWordSpec with Matchers {
  
  private val valuation =
    Valuation(
      assessmentRef = 1L,
      assessmentStatus = "CURRENT",
      rateableValue = Some(BigDecimal(1000)),
      scatCode = None,
      effectiveDate = LocalDate.of(2023, 4, 1),
      currentFromDate = LocalDate.of(2023, 4, 1),
      currentToDate = None,
      descriptionText = "Shop",
      listYear = "2023",
      primaryDescription = "Retail",
      allowedActions = Nil,
      listType = "Local"
    )
  
  private val postcodeProperty =
    PostcodeProperty(
      uarn = 123456L,
      addressFull = "1 Test Street, Test Town, AA1 1AA",
      localAuthorityCode = "AUTH",
      localAuthorityReference = "REF123",
      valuations = List(valuation)
    )
  
  "PostcodeProperties" should {
    "serialise to JSON and deserialise back again" in {
      val model =
        PostcodeProperties(
          total = 1,
          properties = List(postcodeProperty),
          hasNext = false,
          hasPrevious = false
        )

      Json.toJson(model).as[PostcodeProperties] mustEqual model
    }
    
    "deserialise from JSON" in {
      val json =
        Json.obj(
          "total" -> 1,
          "properties" -> Json.arr(
            Json.toJson(postcodeProperty)
          ),
          "hasNext" -> false,
          "hasPrevious" -> false
        )

      val expected =
        PostcodeProperties(
          total = 1,
          properties = List(postcodeProperty),
          hasNext = false,
          hasPrevious = false
        )

      json.as[PostcodeProperties] mustEqual expected
    }

    "default hasNext and hasPrevious to true" in {
      val model =
        PostcodeProperties(
          total = 1,
          properties = List(postcodeProperty)
        )
      
      model.hasNext mustBe true
      model.hasPrevious mustBe true
    }
  }
}