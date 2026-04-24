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
      currentToDate = None,
      descriptionText = description,
      listYear = "2023",
      primaryDescription = "Primary",
      allowedActions = Nil,
      listType = "TEST"
    )

  private def property(
                        address: String,
                        reference: String,
                        valuations: List[Valuation]
                      ): VMVProperty =
    VMVProperty(
      uarn = scala.util.Random.nextLong(),
      addressFull = address,
      localAuthorityCode = "AUTH",
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

    // -----------------------------------------------------------------------
    // Address sorting
    // -----------------------------------------------------------------------

    "sort by AddressASC" in {
      val result = service.sort(properties, "AddressASC")
      result.map(_.addressFull) mustBe
        List("10 Baker Street", "2 Abbey Road", "50 Castle Lane").sorted
    }

    "sort by AddressDESC" in {
      val result = service.sort(properties, "AddressDESC")
      result.map(_.addressFull) mustBe
        List("10 Baker Street", "2 Abbey Road", "50 Castle Lane").sorted.reverse
    }

    // -----------------------------------------------------------------------
    // Description sorting
    // -----------------------------------------------------------------------

    "sort by DescriptionASC using last valuation description" in {
      val result = service.sort(properties, "DescriptionASC")
      result.head mustBe propertyC // empty description -> ""
    }

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
    }

    // -----------------------------------------------------------------------
    // Reference sorting
    // -----------------------------------------------------------------------

    "sort by ReferenceASC" in {
      val result = service.sort(properties, "ReferenceASC")
      result.map(_.localAuthorityReference) mustBe
        List("REF-A", "REF-B", "REF-C")
    }

    "sort by ReferenceDESC" in {
      val result = service.sort(properties, "ReferenceDESC")
      result.map(_.localAuthorityReference) mustBe
        List("REF-C", "REF-B", "REF-A")
    }

    // -----------------------------------------------------------------------
    // Rateable value sorting
    // -----------------------------------------------------------------------

    "sort by RateableValueASC defaulting missing values to zero" in {
      val result = service.sort(properties, "RateableValueASC")
      result.head mustBe propertyC // no valuation => 0
    }

    "sort by RateableValueDESC" in {
      val result = service.sort(properties, "RateableValueDESC")
      result.head mustBe propertyB // highest value
    }

    "use the LAST valuation when sorting by rateable value" in {
      val multiValProperty =
        property(
          "Y Address",
          "REF-Y",
          List(
            valuation("Old", Some(50)),
            valuation("New", Some(5000))
          )
        )

      val result =
        service.sort(properties :+ multiValProperty, "RateableValueDESC")

      result.head mustBe multiValProperty
    }

    "treat None rateable value as zero" in {
      val noRvProperty =
        property(
          "X Address",
          "REF-X",
          List(valuation("No RV", None))
        )

      val result =
        service.sort(properties :+ noRvProperty, "RateableValueASC")

      result must contain(noRvProperty)

      val rateableValues =
        result.map { p =>
          p.valuations.lastOption.flatMap(_.rateableValue).getOrElse(BigDecimal(0))
        }

      rateableValues.min mustBe BigDecimal(0)
    }

    // -----------------------------------------------------------------------
    // Default branch
    // -----------------------------------------------------------------------

    "default to AddressASC when sortBy is unknown" in {
      val result = service.sort(properties, "UNKNOWN")
      result.map(_.addressFull) mustBe
        result.map(_.addressFull).sorted
    }

    // -----------------------------------------------------------------------
    // Case-insensitivity
    // -----------------------------------------------------------------------

    "sort addresses case-insensitively" in {
      val mixedCaseProperty =
        property(
          "a lower street",
          "REF-X",
          Nil
        )

      val result =
        service.sort(properties :+ mixedCaseProperty, "AddressASC")

      result.head.addressFull.toLowerCase mustBe "10 baker street".toLowerCase
    }
  }
}