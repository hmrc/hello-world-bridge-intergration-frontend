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

package views


import helpers.ViewBaseSpec
import models.properties.{VMVProperties, VMVProperty, Valuation}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.html.PropertyResultsView

import java.time.LocalDate

class PropertyResultsViewSpec extends ViewBaseSpec {

  lazy val view: PropertyResultsView = inject[PropertyResultsView]



  def testValuation(
                     description: String,
                     rv: BigDecimal
                   ): Valuation =
    Valuation(
      assessmentRef = 1L,
      assessmentStatus = "CURRENT",
      rateableValue = Some(rv),
      scatCode = None,
      effectiveDate = LocalDate.of(2023, 4, 1),
      currentFromDate = LocalDate.of(2023, 4, 1),
      currentToDate = None,
      descriptionText = description,
      listYear = "2023",
      primaryDescription = "Primary",
      allowedActions = List.empty,
      listType = "Local"
    )


  val postcode = "BH1 1HU"

  val valuation1 = testValuation("Shop", 1000)
  val valuation2 = testValuation("Office", 2000)

  val property1 = VMVProperty(
    addressFull = s"0 test $postcode",
    uarn = 1L,
    localAuthorityCode = "test",
    localAuthorityReference = "test",
    valuations = List(valuation1)
  )

  val property2 = VMVProperty(
    addressFull = s"1 test $postcode",
    uarn = 2L,
    localAuthorityCode = "test",
    localAuthorityReference = "test",
    valuations = List(valuation2)
  )

  val vmv = VMVProperties(
    properties = List(property1, property2),
    total = 2
  )

  val currentPage = 1
  val pageSize = 10
  val sortBy = "AddressASC"

  lazy val html: Html =
    view(vmv, vmv.properties, currentPage, vmv.total, pageSize, sortBy)

  implicit lazy val document: Document = Jsoup.parse(html.body)


  object Selectors {
    val h1Title = "h1.govuk-heading-l"
    val h2Count = "h2.govuk-heading-l"
    val tableRows = "table.govuk-table tbody tr"
    val searchAgainLink =
      s"""a.govuk-link[href="${controllers.routes.FindAPropertyController.onPageLoad().url}"]"""
    val pagination = ".govuk-pagination"
    val sortLabel = "label.govuk-label"
    val applyButton = "button.govuk-button"
    val detailsSummary = ".govuk-details__summary-text"
  }

  "PropertyResultsView" must {

    "produce identical output for apply() and render()" in {
      val htmlApply = view.apply(vmv, vmv.properties, currentPage, vmv.total, pageSize, sortBy).body
      val htmlRender = view.render(vmv, vmv.properties, currentPage, vmv.total, pageSize, sortBy, request, messages).body

      htmlApply mustBe htmlRender
    }

    "f() must not be empty" in {
      view.f(vmv, vmv.properties, currentPage, vmv.total, pageSize, sortBy).toString must not be empty
    }

    "show the correct page title" in {
      document.title must include(s"Search results for $postcode")
    }

    "show the correct main heading (postcode)" in {
      elementText(Selectors.h1Title) mustBe s"Search results for $postcode"
    }

    "show the correct secondary heading (property count)" in {
      elementText(Selectors.h2Count) mustBe "2 properties found"
    }

    "display the correct number of table rows" in {
      document.select(Selectors.tableRows).size mustBe 2
    }

    "display correct data in the first row" in {
      val row = document.select(Selectors.tableRows).get(0)
      row.text must include("0 test BH1 1HU")
    }

    "display correct data in the second row" in {
      val row = document.select(Selectors.tableRows).get(1)
      row.text must include("1 test BH1 1HU")
    }

    "show the sort label" in {
      elementText(Selectors.sortLabel) mustBe "Sort results by"
    }

    "show the Apply button" in {
      elementText(Selectors.applyButton) mustBe "Apply"
    }

    "show the Search again link" in {
      elementText(Selectors.searchAgainLink) mustBe "Search again"
    }

    "show the details summary text" in {
      elementText(Selectors.detailsSummary) mustBe "Help if you cannot find your property"
    }

    "show the pagination component" in {
      document.select(Selectors.pagination).size mustBe 1
    }
  }
}

