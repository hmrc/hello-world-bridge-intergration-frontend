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

import models.properties.{VMVProperty, Valuation}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate

class SortingVMVPropertiesServiceSpec extends AnyWordSpec with Matchers {

  private val service = new SortingVMVPropertiesService()
  private val today = LocalDate.now()

  private def valuation(
                         description: String,
                         rateableValue: Option[BigDecimal]
                       ): Valuation =
    Valuation(
      assessmentRef = 1L,
      assessmentStatus = "CURRENT",
      rateableValue = rateableValue,
      scatCode = None,
      effectiveDate = today,
      currentFromDate = today,
      currentToDate = None,
      descriptionText = description,
      listYear = "2023",
      primaryDescription = description,
      allowedActions = Nil,
      listType = "LOCAL"
    )

  private def property(
                        address: String,
                        reference: String,
                        description: Option[String] = None,
                        rateableValue: Option[BigDecimal] = None
                      ): VMVProperty =
    VMVProperty(
      uarn = 123123123L,
      addressFull = address,
      localAuthorityCode = "123",
      localAuthorityReference = reference,
      valuations = description.orElse(rateableValue)
        .toList
        .map(_ => valuation(description.getOrElse(""), rateableValue))
    )

  "SortingVMVPropertiesService.sort" should {
    "sort properties by address ascending (case insensitive)" in {
      val properties = List(
        property("Zebra Street", "REF3"),
        property("alpha road", "REF1"),
        property("Beta Avenue", "REF2")
      )

      val result = service.sort(properties, "AddressASC")
      result.map(_.addressFull) shouldBe List(
        "alpha road",
        "Beta Avenue",
        "Zebra Street"
      )
    }

    "sort properties by address descending" in {
      val properties = List(
        property("Zebra Street", "REF3"),
        property("alpha road", "REF1"),
        property("Beta Avenue", "REF2")
      )

      val result = service.sort(properties, "AddressDESC")
      result.map(_.addressFull) shouldBe List(
        "Zebra Street",
        "Beta Avenue",
        "alpha road"
      )
    }

    "sort properties by description ascending, safely handling missing valuations" in {
      val properties = List(
        property("A", "REF1", description = Some("Warehouse")),
        property("B", "REF2"),
        property("C", "REF3", description = Some("Office"))
      )

      val result = service.sort(properties, "DescriptionASC")
      result.map(_.localAuthorityReference) shouldBe List(
        "REF2", // no valuation -> ""
        "REF3", // Office
        "REF1"  // Warehouse
      )
    }

    "sort properties by description descending, safely handling missing valuations" in {
      val properties = List(
        property("A", "REF1", description = Some("Warehouse")),
        property("B", "REF2"),
        property("C", "REF3", description = Some("Office"))
      )

      val result = service.sort(properties, "DescriptionDESC")
      result.map(_.localAuthorityReference) shouldBe List(
        "REF1",
        "REF3",
        "REF2"
      )
    }

    "sort properties by reference ascending, defaulting missing values to zero" in {
      val properties = List(
        property("A", "REF1"),
        property("B", "REF2"),
        property("C", "REF3")
      )

      val result = service.sort(properties, "ReferenceASC")

      result.map(_.localAuthorityReference) shouldBe List(
        "REF1",
        "REF2",
        "REF3"
      )
    }

    "sort properties by reference descending, defaulting missing values to zero" in {
      val properties = List(
        property("A", "REF1"),
        property("B", "REF2"),
        property("C", "REF3")
      )

      val result = service.sort(properties, "ReferenceDESC")

      result.map(_.localAuthorityReference) shouldBe List(
        "REF3",
        "REF2",
        "REF1"
      )
    }

    "sort properties by rateable value ascending, defaulting missing values to zero" in {
      val properties = List(
        property("A", "REF1", rateableValue = Some(BigDecimal(1000))),
        property("B", "REF2"),
        property("C", "REF3", rateableValue = Some(BigDecimal(500)))
      )

      val result = service.sort(properties, "RateableValueASC")

      result.map(_.localAuthorityReference) shouldBe List(
        "REF2",
        "REF3",
        "REF1" // missing RV -> 0
      )
    }

    "sort properties by rateable value descending, defaulting missing values to zero" in {
      val properties = List(
        property("A", "REF1", rateableValue = Some(BigDecimal(1000))),
        property("B", "REF2"),
        property("C", "REF3", rateableValue = Some(BigDecimal(500)))
      )

      val result = service.sort(properties, "RateableValueDESC")

      result.map(_.localAuthorityReference) shouldBe List(
        "REF1",
        "REF3",
        "REF2" // missing RV -> 0
      )
    }

    "default to address ascending when sortBy is unrecognised" in {
      val properties = List(
        property("B Street", "REF2"),
        property("A Street", "REF1")
      )

      val result = service.sort(properties, "UNKNOWN")

      result.map(_.addressFull) shouldBe List(
        "A Street",
        "B Street"
      )
    }
  }
}