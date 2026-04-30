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
import connectors.BridgeIntegrationConnector
import helpers.TestData
import models.assessment.AssessmentProperties
import models.properties.RatepayerPropertyLinksResponse
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.CSRFTokenHelper.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import service.AssessmentPropertiesSortingService
import views.html.PropertiesForAssessment

import scala.concurrent.Future

class PropertiesControllerSpec
  extends SpecBase
    with MockitoSugar 
    with TestData {

  private val mockConnector = mock[BridgeIntegrationConnector]
  private val mockSorting   = mock[AssessmentPropertiesSortingService]


  private val context =
    RatepayerPropertyLinksResponse(
      properties = List(testProperty),
      persons = List.empty,
      relationships = List.empty
    )

  private def application =
    applicationBuilder(None)
      .overrides(
        bind[BridgeIntegrationConnector].toInstance(mockConnector),
        bind[AssessmentPropertiesSortingService].toInstance(mockSorting)
      )
      .build()

   def beforeEach(): Unit =
    reset(mockConnector, mockSorting)

  // =========================================================
  // PropertiesController
  // =========================================================

  "PropertiesController" - {

    // ---------------------------------------------------------
    // onPageLoad
    // ---------------------------------------------------------

    "onPageLoad" - {

      "must return OK and render the PropertiesForAssessment view" in {

        when(mockConnector.getRatepayerProperties(any())(any()))
          .thenReturn(Future.successful(Some(context)))

        when(mockSorting.sort(any(), any()))
          .thenAnswer(_.getArgument(0))

        val app = application
        val view = app.injector.instanceOf[PropertiesForAssessment]

        val request =
          FakeRequest(
            GET,
            routes.PropertiesController.onPageLoad().url
          ).withCSRFToken

        val result = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result) mustBe view(
          AssessmentProperties(List(testProperty)),
          1, // currentPage
          1, // totalProperties
          100, // pageSize
          "AddressASC"
        )(request, messages(app)).toString

        app.stop()
      }

      "must apply page and sortBy query parameters" in {

        when(mockConnector.getRatepayerProperties(any())(any()))
          .thenReturn(Future.successful(Some(context)))

        when(mockSorting.sort(any(), any()))
          .thenAnswer(_.getArgument(0))

        val app = application

        val request =
          FakeRequest(
            GET,
            routes.PropertiesController.onPageLoad().url + "?page=2&sortBy=RateableValueDESC"
          ).withCSRFToken

        val result = route(app, request).value

        status(result) mustEqual OK

        verify(mockSorting)
          .sort(any(), eqTo("RateableValueDESC"))

        app.stop()
      }

      "must return NOT_FOUND when no properties are returned" in {

        when(mockConnector.getRatepayerProperties(any())(any()))
          .thenReturn(Future.successful(None))

        val app = application

        val request =
          FakeRequest(
            GET,
            routes.PropertiesController.onPageLoad().url
          ).withCSRFToken

        val result = route(app, request).value

        status(result) mustEqual NOT_FOUND
        contentAsString(result) must include ("No properties found")

        app.stop()
      }

      "must return INTERNAL_SERVER_ERROR when the connector throws" in {

        when(mockConnector.getRatepayerProperties(any())(any()))
          .thenReturn(Future.failed(new RuntimeException("boom")))

        val app = application

        val request =
          FakeRequest(
            GET,
            routes.PropertiesController.onPageLoad().url
          ).withCSRFToken

        val result = route(app, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
        contentAsString(result) must include ("Failed to load assessment properties")

        app.stop()
      }
    }

    // ---------------------------------------------------------
    // sort
    // ---------------------------------------------------------

    "sort" - {

      "must redirect to onPageLoad with the selected sortBy" in {

        val app = application

        val request =
          FakeRequest(
            POST,
            routes.PropertiesController.sort().url
          ).withFormUrlEncodedBody(
            "sortBy" -> "RateableValueASC"
          ).withCSRFToken

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.PropertiesController.onPageLoad().url + "?page=1&sortBy=RateableValueASC"

        app.stop()
      }

      "must default sortBy when not supplied" in {

        val app = application

        val request =
          FakeRequest(
            POST,
            routes.PropertiesController.sort().url
          ).withFormUrlEncodedBody().withCSRFToken

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.PropertiesController.onPageLoad().url + "?page=1&sortBy=AddressASC"

        app.stop()
      }
    }
  }

  // =========================================================
  // RatepayerPropertyLinksResponse JSON format
  // =========================================================

  "RatepayerPropertyLinksResponse JSON format" - {

    "must round‑trip to and from JSON" in {

      val model =
        RatepayerPropertyLinksResponse(
          properties = List.empty,
          persons = List.empty,
          relationships = List.empty
        )

      val json = Json.toJson(model)
      json.as[RatepayerPropertyLinksResponse] mustBe model
    }
  }
}