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
import models.bridge.search.{PostcodeSearchResult, RecordWrapper}
import models.properties.{PostcodeProperties, StoredPostcodeSearchResults}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.FindAPropertyBridgeRepo
import service.SortingBridgePropertiesService

import scala.concurrent.Future

class PropertyResultsBridgeControllerSpec
  extends SpecBase
    with MockitoSugar {

  private val mockRepo = mock[FindAPropertyBridgeRepo]
  private val mockSorting = mock[SortingBridgePropertiesService]

  private def application =
    applicationBuilder(None)
      .overrides(
        inject.bind[FindAPropertyBridgeRepo].toInstance(mockRepo),
        inject.bind[SortingBridgePropertiesService].toInstance(mockSorting)
      )
      .build()

  private val record1 = mock[RecordWrapper]
  private val record2 = mock[RecordWrapper]

  private val records: List[RecordWrapper] =
    List(record1, record2)

  private def storedWithRecords(
                                 records: List[RecordWrapper]
                               ): (StoredPostcodeSearchResults, PostcodeSearchResult) = {

    val postcodeProperties = mock[PostcodeProperties]
    val postcodeSearchResult = mock[PostcodeSearchResult]
    val stored = mock[StoredPostcodeSearchResults]

    when(postcodeProperties.properties).thenReturn(records)

    when(postcodeSearchResult.results).thenReturn(postcodeProperties)

    when(stored.result).thenReturn(postcodeSearchResult)

    (stored, postcodeSearchResult)
  }

  "PropertyResultsBridgeController onPageLoad" - {

    "redirect back to PropertyResultsBridge when no stored data exists" in {
      val app = application
      when(mockRepo.findByUserId(any()))
        .thenReturn(Future.successful(None))

      val request =
        FakeRequest(GET, routes.PropertyResultsBridgeController.selectProperty(0, "AddressASC").url).withCSRFToken

      val result =
        route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.PropertyResultsBridgeController.onPageLoad().url
      app.stop()
    }
  }
}