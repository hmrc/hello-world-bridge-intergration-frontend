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
import models.UserAnswers
import models.properties.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.CSRFTokenHelper.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.FindAPropertyBridgeRepo
import services.SortingPostcodeAddressResultsService
import views.html.PropertyResultsBridgeView

import java.time.Instant
import scala.concurrent.Future

class PropertyResultsBridgeControllerSpec
  extends SpecBase
    with MockitoSugar
    with BeforeAndAfterEach {

  private val mockRepo = mock[FindAPropertyBridgeRepo]
  private val mockSorting = mock[SortingPostcodeAddressResultsService]

  private val sortBy = "address"

  private val recordOne =
    Record(
      list = ValuationList(
        id = "list-1",
        classification = Classification(
          code = "C",
          label = "Council Tax"
        ),
        collection_authority = CollectionAuthority(
          ons_code = "E07000192",
          ons_code_label = "Test Council"
        )
      ),
      list_entry = ListEntry(
        relevant_property = RelevantProperty(
          id = "property-1"
        ),
        addresses = Addresses(
          property_full_address = "1 Test Street, Test Town, TT1 1TT"
        ),
        valuation = Valuation(
          value = "A"
        )
      )
    )

  private val recordTwo =
    Record(
      list = ValuationList(
        id = "list-2",
        classification = Classification(
          code = "C",
          label = "Council Tax"
        ),
        collection_authority = CollectionAuthority(
          ons_code = "E07000192",
          ons_code_label = "Test Council"
        )
      ),
      list_entry = ListEntry(
        relevant_property = RelevantProperty(
          id = "property-2"
        ),
        addresses = Addresses(
          property_full_address = "2 Test Street, Test Town, TT1 1TT"
        ),
        valuation = Valuation(
          value = "B"
        )
      )
    )

  private val baseProperties =
    PostcodeSearchResult(
      results = Results(
        current_page = 1,
        page_size = 10,
        total_results = 2,
        total_pages = 1,
        has_next = false,
        has_previous = false,
        self = "/self",
        next = None,
        prev = None,
        first = "/first",
        last = "/last",
        records = Seq(recordOne, recordTwo)
      )
    )

  private val storedProperties =
    NewStoredVMVProperties(
      userId = "test-user-id",
      properties = baseProperties,
      createdAt = Instant.parse("2026-01-01T00:00:00Z")
    )

  private val twelveRecords =
    List(
      recordOne,
      recordTwo,
      recordOne,
      recordTwo,
      recordOne,
      recordTwo,
      recordOne,
      recordTwo,
      recordOne,
      recordTwo,
      recordOne,
      recordTwo
    )

  private val storedPropertiesWithTwelveRecords =
    storedProperties.copy(
      properties = storedProperties.properties.copy(
        results = storedProperties.properties.results.copy(
          records = twelveRecords
        )
      )
    )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRepo, mockSorting)
  }

  private def applicationWithAnswers(userAnswers: Option[UserAnswers]) =
    applicationBuilder(userAnswers)
      .overrides(
        bind[FindAPropertyBridgeRepo].toInstance(mockRepo),
        bind[SortingPostcodeAddressResultsService].toInstance(mockSorting)
      )
      .build()

  "PropertyResultsBridgeController" - {

    "onPageLoad" - {

      "must return OK and render the first page of property results when stored results exist" in {
        val application = applicationWithAnswers(Some(emptyUserAnswers))

        val sortedRecords =
          twelveRecords

        val expectedPageRecords =
          sortedRecords.take(10)

        val expectedPagedProperties =
          storedPropertiesWithTwelveRecords.properties.copy(
            results = storedPropertiesWithTwelveRecords.properties.results.copy(
              current_page = 1,
              page_size = 10,
              total_results = 12,
              total_pages = 2,
              has_next = true,
              has_previous = false,
              records = expectedPageRecords
            )
          )

        when(mockRepo.findByUserId(any()))
          .thenReturn(Future.successful(Some(storedPropertiesWithTwelveRecords)))

        when(mockSorting.sort(eqTo(twelveRecords), eqTo(sortBy)))
          .thenReturn(sortedRecords)

        val request =
          FakeRequest(GET, routes.PropertyResultsBridgeController.onPageLoad(1, sortBy).url)
            .withCSRFToken

        val result = route(application, request).value
        val view = application.injector.instanceOf[PropertyResultsBridgeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(expectedPagedProperties, sortBy)(request, messages(application)).toString

        verify(mockRepo, times(1)).findByUserId(any())
        verify(mockSorting, times(1)).sort(eqTo(twelveRecords), eqTo(sortBy))

        application.stop()
      }

      "must return OK and render the second page of property results" in {
        val application = applicationWithAnswers(Some(emptyUserAnswers))

        val sortedRecords =
          twelveRecords

        val expectedPageRecords =
          sortedRecords.slice(10, 20)

        val expectedPagedProperties =
          storedPropertiesWithTwelveRecords.properties.copy(
            results = storedPropertiesWithTwelveRecords.properties.results.copy(
              current_page = 2,
              page_size = 10,
              total_results = 12,
              total_pages = 2,
              has_next = false,
              has_previous = true,
              records = expectedPageRecords
            )
          )

        when(mockRepo.findByUserId(any()))
          .thenReturn(Future.successful(Some(storedPropertiesWithTwelveRecords)))

        when(mockSorting.sort(eqTo(twelveRecords), eqTo(sortBy)))
          .thenReturn(sortedRecords)

        val request =
          FakeRequest(GET, routes.PropertyResultsBridgeController.onPageLoad(2, sortBy).url)
            .withCSRFToken

        val result = route(application, request).value
        val view = application.injector.instanceOf[PropertyResultsBridgeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(expectedPagedProperties, sortBy)(request, messages(application)).toString

        verify(mockRepo, times(1)).findByUserId(any())
        verify(mockSorting, times(1)).sort(eqTo(twelveRecords), eqTo(sortBy))

        application.stop()
      }

      "must default to page 1 when requested page is less than 1" in {
        val application = applicationWithAnswers(Some(emptyUserAnswers))

        val sortedRecords =
          twelveRecords

        val expectedPageRecords =
          sortedRecords.take(10)

        val expectedPagedProperties =
          storedPropertiesWithTwelveRecords.properties.copy(
            results = storedPropertiesWithTwelveRecords.properties.results.copy(
              current_page = 1,
              page_size = 10,
              total_results = 12,
              total_pages = 2,
              has_next = true,
              has_previous = false,
              records = expectedPageRecords
            )
          )

        when(mockRepo.findByUserId(any()))
          .thenReturn(Future.successful(Some(storedPropertiesWithTwelveRecords)))

        when(mockSorting.sort(eqTo(twelveRecords), eqTo(sortBy)))
          .thenReturn(sortedRecords)

        val request =
          FakeRequest(GET, routes.PropertyResultsBridgeController.onPageLoad(0, sortBy).url)
            .withCSRFToken

        val result = route(application, request).value
        val view = application.injector.instanceOf[PropertyResultsBridgeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(expectedPagedProperties, sortBy)(request, messages(application)).toString

        application.stop()
      }

      "must default to the last page when requested page is greater than total pages" in {
        val application = applicationWithAnswers(Some(emptyUserAnswers))

        val sortedRecords =
          twelveRecords

        val expectedPageRecords =
          sortedRecords.slice(10, 20)

        val expectedPagedProperties =
          storedPropertiesWithTwelveRecords.properties.copy(
            results = storedPropertiesWithTwelveRecords.properties.results.copy(
              current_page = 2,
              page_size = 10,
              total_results = 12,
              total_pages = 2,
              has_next = false,
              has_previous = true,
              records = expectedPageRecords
            )
          )

        when(mockRepo.findByUserId(any()))
          .thenReturn(Future.successful(Some(storedPropertiesWithTwelveRecords)))

        when(mockSorting.sort(eqTo(twelveRecords), eqTo(sortBy)))
          .thenReturn(sortedRecords)

        val request =
          FakeRequest(GET, routes.PropertyResultsBridgeController.onPageLoad(99, sortBy).url)
            .withCSRFToken

        val result = route(application, request).value
        val view = application.injector.instanceOf[PropertyResultsBridgeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(expectedPagedProperties, sortBy)(request, messages(application)).toString

        application.stop()
      }

      "must redirect to FindAPropertyController when no stored results exist" in {
        val application = applicationWithAnswers(Some(emptyUserAnswers))

        when(mockRepo.findByUserId(any()))
          .thenReturn(Future.successful(None))

        val request =
          FakeRequest(GET, routes.PropertyResultsBridgeController.onPageLoad(1, sortBy).url)
            .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.FindAPropertyController.onPageLoad().url

        verify(mockRepo, times(1)).findByUserId(any())
        verifyNoInteractions(mockSorting)

        application.stop()
      }
    }

    "selectProperty" - {

      "must redirect to FindAPropertyController when a valid property index is selected" in {
        val application = applicationWithAnswers(Some(emptyUserAnswers))

        when(mockRepo.findByUserId(any()))
          .thenReturn(Future.successful(Some(storedPropertiesWithTwelveRecords)))

        when(mockSorting.sort(eqTo(twelveRecords), eqTo(sortBy)))
          .thenReturn(twelveRecords)

        val request =
          FakeRequest(GET, routes.PropertyResultsBridgeController.selectProperty(0, sortBy).url)
            .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.FindAPropertyController.onPageLoad().url

        verify(mockRepo, times(1)).findByUserId(any())
        verify(mockSorting, times(1)).sort(eqTo(twelveRecords), eqTo(sortBy))

        application.stop()
      }

      "must redirect back to property results when selected index does not exist" in {
        val application = applicationWithAnswers(Some(emptyUserAnswers))

        when(mockRepo.findByUserId(any()))
          .thenReturn(Future.successful(Some(storedPropertiesWithTwelveRecords)))

        when(mockSorting.sort(eqTo(twelveRecords), eqTo(sortBy)))
          .thenReturn(twelveRecords)

        val request =
          FakeRequest(GET, routes.PropertyResultsBridgeController.selectProperty(999, sortBy).url)
            .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.PropertyResultsController.onPageLoad(1, sortBy).url

        verify(mockRepo, times(1)).findByUserId(any())
        verify(mockSorting, times(1)).sort(eqTo(twelveRecords), eqTo(sortBy))

        application.stop()
      }

      "must redirect to FindAPropertyController when no stored results exist" in {
        val application = applicationWithAnswers(Some(emptyUserAnswers))

        when(mockRepo.findByUserId(any()))
          .thenReturn(Future.successful(None))

        val request =
          FakeRequest(GET, routes.PropertyResultsBridgeController.selectProperty(0, sortBy).url)
            .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.FindAPropertyController.onPageLoad().url

        verify(mockRepo, times(1)).findByUserId(any())
        verifyNoInteractions(mockSorting)

        application.stop()
      }
    }
  }
}