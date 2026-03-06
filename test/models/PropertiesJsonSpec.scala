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

package models

import models.properties._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._
import java.time.{Instant, LocalDate}

class PropertiesJsonSpec extends AnyFreeSpec with Matchers {

  "Valuation JSON format" - {
    "must serialize and deserialize correctly" in {
      val valuation = Valuation(
        assessmentRef = 123L,
        assessmentStatus = "",
        rateableValue = Some(BigDecimal(1)),
        scatCode = Some(""),
        effectiveDate = LocalDate.of(2020, 1, 1),
        currentFromDate = LocalDate.of(2020, 4, 1),
        currentToDate = Some(LocalDate.of(2021, 4, 1)),
        descriptionText = "",
        listYear = "",
        primaryDescription = "",
        allowedActions = List(""),
        listType = ""
      )

      val json = Json.toJson(valuation)
      json.as[Valuation] mustEqual valuation
    }
  }

  "VMVProperty JSON format" - {
    "must serialize and deserialize correctly" in {
      val valuation = Valuation(
        1L, "LIVE", None, None,
        LocalDate.now(), LocalDate.now(), None,
        "", "", "", Nil, ""
      )

      val property = VMVProperty(
        uarn = 123L,
        addressFull = "",
        localAuthorityCode = "",
        localAuthorityReference = "",
        valuations = List(valuation)
      )

      val json = Json.toJson(property)
      json.as[VMVProperty] mustEqual property
    }
  }

  "VMVProperties JSON format" - {
    "must serialize and deserialize correctly" in {
      val props = VMVProperties(
        total = 1,
        properties = List(
          VMVProperty(123L, "", "", "", Nil)
        ),
        hasNext = false,
        hasPrevious = true
      )

      val json = Json.toJson(props)
      json.as[VMVProperties] mustEqual props
    }
  }

  "StoredVMVProperties JSON format" - {
    "must serialize and deserialize correctly" in {
      val now = Instant.now()

      val stored = StoredVMVProperties(
        userId = "",
        properties = VMVProperties(0, Nil),
        createdAt = now
      )

      val json = Json.toJson(stored)
      json.as[StoredVMVProperties] mustEqual stored
    }
  }
}

