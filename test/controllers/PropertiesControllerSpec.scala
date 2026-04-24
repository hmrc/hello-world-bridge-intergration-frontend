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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.*
import play.api.inject.bind
import play.api.inject
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import views.html.PropertiesForAssessment
import models.assessment.*

import scala.concurrent.Future

class PropertiesControllerSpec
  extends SpecBase
    with MockitoSugar {

  private val mockConnector = mock[BridgeIntegrationConnector]

  private def application =
    applicationBuilder(None)
      .overrides(
        bind[BridgeIntegrationConnector].toInstance(mockConnector)
      )
      .build()

  private val credId = "123"
  private val assessmentId = "456"

  "PropertiesController.onPageLoad" - {

    "must return OK and render the view when connector returns data" in {
      val app = application

      val responseModel = AssessmentPropertiesResponse(
        List(
          AssessmentProperty(
            address = "10 Test Street",
            foreignId = "ABC123",
            laCode = "E123",
            description = "Shop",
            rateableValue = 1000
          )
        )
      )

      when(mockConnector.getPropertiesForAssessment(any(), any())(any()))
        .thenReturn(Future.successful(responseModel))

      val request =
        FakeRequest(GET, routes.PropertiesController.onPageLoad(credId, assessmentId).url)
          .withCSRFToken

      val result = route(app, request).value
      val view = app.injector.instanceOf[PropertiesForAssessment]

      val expectedHtml = view(
        AssessmentProperties(responseModel.properties),
        currentPage = 1,
        totalProperties = responseModel.properties.size,
        pageSize = 100,
        sortBy = "AddressASC",
        credId = credId,
        assessmentId = assessmentId
      )(request, messages(app)).toString

      status(result) mustEqual OK
      contentAsString(result) mustEqual expectedHtml

      app.stop()
    }

    "must return INTERNAL_SERVER_ERROR when connector throws" in {
      val app = application

      when(mockConnector.getPropertiesForAssessment(any(), any())(any()))
        .thenReturn(Future.failed(new RuntimeException("test crash")))

      val request =
        FakeRequest(GET, routes.PropertiesController.onPageLoad(credId, assessmentId).url)
          .withCSRFToken

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      app.stop()
    }
  }
}