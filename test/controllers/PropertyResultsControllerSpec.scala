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

package controllers

import base.SpecBase
import models.properties.{StoredVMVProperties, VMVProperties, VMVProperty, Valuation}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.FindAPropertyRepo
import service.SortingVMVPropertiesService
import views.html.PropertyResultsView
import play.api.inject
import play.api.test.CSRFTokenHelper.CSRFRequest

import java.time.LocalDate
import scala.concurrent.Future

class PropertyResultsControllerSpec
  extends SpecBase
    with MockitoSugar {

  private val mockRepo     = mock[FindAPropertyRepo]
  private val mockSorting  = mock[SortingVMVPropertiesService]

  private def application =
    applicationBuilder(None)
      .overrides(
        inject.bind[FindAPropertyRepo].toInstance(mockRepo),
        inject.bind[SortingVMVPropertiesService].toInstance(mockSorting)
      )
      .build()


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

  val valuation1: Valuation = testValuation("Shop", 1000)
  val valuation2: Valuation = testValuation("Office", 2000)

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

   val stored = StoredVMVProperties(
    userId = userAnswersId,
    properties = vmv
  )


  "onPageLoad" - {

    "return OK and render the view when data exists" in {
      val app = application

      when(mockRepo.findByUserId(any())).thenReturn(Future.successful(Some(stored)))
      when(mockSorting.sort(any(), any())).thenReturn(List(property1, property2))

      val request = FakeRequest(GET, routes.PropertyResultsController.onPageLoad(1, "AddressASC").url).withCSRFToken
      val result  = route(app, request).value

      val view = app.injector.instanceOf[PropertyResultsView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual
        view(vmv, List(property1, property2), 1, 2, 10, "AddressASC")(request, messages(app)).toString

      app.stop()
    }

    "redirect to FindAProperty when no stored data exists" in {
      val app = application

      when(mockRepo.findByUserId(any())).thenReturn(Future.successful(None))

      val request = FakeRequest(GET, routes.PropertyResultsController.onPageLoad(1, "AddressASC").url).withCSRFToken
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.FindAPropertyController.onPageLoad().url

      app.stop()
    }
  }


  "sort" - {

    "redirect to page 1 with selected sortBy value" in {
      val app = application

      val request =
        FakeRequest(POST, routes.PropertyResultsController.sort.url)
          .withFormUrlEncodedBody("sortBy" -> "DescriptionDESC")
          .withCSRFToken

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual
        routes.PropertyResultsController.onPageLoad(1, "DescriptionDESC").url

      app.stop()
    }

    "default to AddressASC when sortBy is missing" in {
      val app = application

      val request =
        FakeRequest(POST, routes.PropertyResultsController.sort.url)
          .withCSRFToken

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual
        routes.PropertyResultsController.onPageLoad(1, "AddressASC").url

      app.stop()
    }
  }


  "selectProperty" - {

    "redirect to correct page" in {
      val app = application

      when(mockRepo.findByUserId(any())).thenReturn(Future.successful(Some(stored)))
      when(mockSorting.sort(any(), any())).thenReturn(List(property1, property2))

      val request = FakeRequest(GET, routes.PropertyResultsController.selectProperty(0, "AddressASC").url).withCSRFToken
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.FindAPropertyController.onPageLoad().url //  TODO !!!

      app.stop()
    }

    "redirect back to results when index is out of range" in {
      val app = application

      when(mockRepo.findByUserId(any())).thenReturn(Future.successful(Some(stored)))
      when(mockSorting.sort(any(), any())).thenReturn(List(property1, property2))

      val request = FakeRequest(GET, routes.PropertyResultsController.selectProperty(99, "AddressASC").url).withCSRFToken
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual
        routes.PropertyResultsController.onPageLoad(1, "AddressASC").url

      app.stop()
    }

    "redirect to FindAProperty when no stored data exists" in {
      val app = application

      when(mockRepo.findByUserId(any())).thenReturn(Future.successful(None))

      val request = FakeRequest(GET, routes.PropertyResultsController.selectProperty(0, "AddressASC").url).withCSRFToken
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.FindAPropertyController.onPageLoad().url

      app.stop()
    }
  }
}


