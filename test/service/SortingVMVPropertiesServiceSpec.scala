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

import models.properties._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate

class SortingVMVPropertiesServiceSpec
  extends AnyWordSpec
    with Matchers {

  private val service = new SortingVMVPropertiesService()
  private val today = LocalDate.now()

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private def valuation(
                         description: String,
                         rateableValue: Option[BigDecimal]
                       ): Valuation =
    Valuation(
      assessmentRef = 1L,
      assessmentStatus = "CURRENT",
      rateableValue = rateableValue,
      scatCode = None,
      effectiveDate = LocalDate.now(),
      currentFromDate = LocalDate.now(),
      effectiveDate = today,
      currentFromDate = today,
      currentToDate = None,
      descriptionText = description,
      listYear = "2023",
      primaryDescription = "Primary",
      primaryDescription = description,
      allowedActions = Nil,
      listType = "TEST"
      listType = "LOCAL"
    )

  private def property(
                        address: String,
                        reference: String,
                        valuations: List[Valuation]
                        description: Option[String] = None,
                        rateableValue: Option[BigDecimal] = None
                      ): VMVProperty =
    VMVProperty(
      uarn = scala.util.Random.nextLong(),
      uarn = 123123123L,
      addressFull = address,
      localAuthorityCode = "AUTH",
      localAuthorityCode = "123",
      localAuthorityReference = reference,
      valuations = valuations
    )

  // ---------------------------------------------------------------------------
  // Base test data
  // ---------------------------------------------------------------------------

  private val propertyA =
    property(
      address = "10 Baker Street",
      reference = "REF-B",
      valuations = List(valuation("Office", Some(1000)))
      valuations = description.orElse(rateableValue)
        .toList
        .map(_ => valuation(description.getOrElse(""), rateableValue))
    )

  private val propertyB =
    property(
      address = "2 Abbey Road",
      reference = "REF-A",
      valuations = List(valuation("Retail", Some(2000)))
    )

  private val propertyC =
    property(
      address = "50 Castle Lane",
      reference = "REF-C",
      valuations = Nil // no valuation
    )

  private val properties = List(propertyA, propertyB, propertyC)

  // ===========================================================================
  // Tests
  // ===========================================================================

  "SortingVMVPropertiesService.sort" should {
    "sort properties by address ascending (case insensitive)" in {
      val properties = List(
        property("Zebra Street", "REF3"),
        property("alpha road", "REF1"),
        property("Beta Avenue", "REF2")
      )

    // -----------------------------------------------------------------------
    // Address sorting
    // -----------------------------------------------------------------------

    "sort by AddressASC" in {
      val result = service.sort(properties, "AddressASC")
      result.map(_.addressFull) mustBe
        List("10 Baker Street", "2 Abbey Road", "50 Castle Lane").sorted
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

    "sort by AddressDESC" in {
      val result = service.sort(properties, "AddressDESC")
      result.map(_.addressFull) mustBe
        List("10 Baker Street", "2 Abbey Road", "50 Castle Lane").sorted.reverse
      result.map(_.addressFull) shouldBe List(
        "Zebra Street",
        "Beta Avenue",
        "alpha road"
      )
    }

    // -----------------------------------------------------------------------
    // Description sorting
    // -----------------------------------------------------------------------
    "sort properties by description ascending, safely handling missing valuations" in {
      val properties = List(
        property("A", "REF1", description = Some("Warehouse")),
        property("B", "REF2"),
        property("C", "REF3", description = Some("Office"))
      )

    "sort by DescriptionASC using last valuation description" in {
      val result = service.sort(properties, "DescriptionASC")
      result.head mustBe propertyC // empty description -> ""
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

    "sort by DescriptionDESC using last valuation description" in {
      val result = service.sort(properties, "DescriptionDESC")
      result.last mustBe propertyC
    }

    "use the LAST valuation when sorting by description" in {
      val multiValProperty =
        property(
          "Z Address",
          "REF-Z",
          List(
            valuation("AAA", Some(1)),
            valuation("ZZZ", Some(2))
          )
        )

      val result =
        service.sort(properties :+ multiValProperty, "DescriptionDESC")

      result.head mustBe multiValProperty
      result.map(_.localAuthorityReference) shouldBe List(
        "REF1",
        "REF3",
        "REF2"
      )
    }

    // -----------------------------------------------------------------------
    // Reference sorting
    // -----------------------------------------------------------------------
    "sort properties by reference ascending, defaulting missing values to zero" in {
      val properties = List(
        property("A", "REF1"),
        property("B", "REF2"),
        property("C", "REF3")
      )

    "sort by ReferenceASC" in {
      val result = service.sort(properties, "ReferenceASC")
      result.map(_.localAuthorityReference) mustBe
        List("REF-A", "REF-B", "REF-C")

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

    "sort by ReferenceDESC" in {
      val result = service.sort(properties, "ReferenceDESC")
      result.map(_.localAuthorityReference) mustBe
        List("REF-C", "REF-B", "REF-A")

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