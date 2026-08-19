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
import connectors.{BridgeIntegrationConnector, FindAPropertyConnector}
import forms.{FindAPropertyBridgeForm, FindAPropertyForm}
import models.properties.{PostcodeProperties, PostcodeProperty, VMVProperties, VMVProperty}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.{FindAPropertyBridgeRepo, FindAPropertyRepo}
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse
import views.html.{FindAPropertyBridgeView, FindAPropertyView}

import scala.concurrent.Future

class FindAPropertyBridgeControllerSpec
  extends SpecBase
    with MockitoSugar {

  private val mockConnector = mock[BridgeIntegrationConnector]
  private val mockRepo = mock[FindAPropertyBridgeRepo]

  private val form = FindAPropertyBridgeForm.form

  private def application =
    applicationBuilder(None)
      .overrides(
        inject.bind[BridgeIntegrationConnector].toInstance(mockConnector),
        inject.bind[FindAPropertyBridgeRepo].toInstance(mockRepo)
      )
      .build()

  "FindAPropertyBridgeController.onPageLoad" - {

    "return OK and render the view" in {
      val app = application
      val request = FakeRequest(GET, routes.FindAPropertyBridgeController.onPageLoad().url).withCSRFToken
      val result = route(app, request).value

      val view = app.injector.instanceOf[FindAPropertyBridgeView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form)(request, messages(app)).toString

      app.stop()
    }
  }

  "FindAPropertyBridgeController.onSubmit" - {

    "return BAD_REQUEST when form is invalid" in {
      val app = application

      val request =
        FakeRequest(POST, routes.FindAPropertyBridgeController.onSubmit().url)
          .withFormUrlEncodedBody("postcode-value" -> "")
          .withCSRFToken

      val result = route(app, request).value
      val view = app.injector.instanceOf[FindAPropertyBridgeView]

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(form.bind(Map("postcode-value" -> "")))(request, messages(app)).toString

      app.stop()
    }

    "return error status when connector returns Left(error)" in {
      val app = application

      val error = ErrorResponse(418, "test response")

      when(mockConnector.postcodeSearch(any(),any())(any()))
        .thenReturn(Future.successful(Left(error)))

      val request =
        FakeRequest(POST, routes.FindAPropertyBridgeController.onSubmit().url)
          .withFormUrlEncodedBody("postcode-value" -> "BS14TB")
          .withCSRFToken

      val result = route(app, request).value

      status(result) mustEqual 418

      app.stop()
    }
  }
}


