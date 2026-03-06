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
import connectors.FindAPropertyConnector
import forms.FindAPropertyForm
import models.properties.{VMVProperties, VMVProperty}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.FindAPropertyRepo
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse
import views.html.FindAPropertyView

import scala.concurrent.Future

class FindAPropertyControllerSpec
  extends SpecBase
    with MockitoSugar {

  private val mockConnector = mock[FindAPropertyConnector]
  private val mockRepo = mock[FindAPropertyRepo]

  private val form = FindAPropertyForm.form

  private def application =
    applicationBuilder(None)
      .overrides(
        inject.bind[FindAPropertyConnector].toInstance(mockConnector),
        inject.bind[FindAPropertyRepo].toInstance(mockRepo)
      )
      .build()

  "FindAPropertyController.onPageLoad" - {

    "return OK and render the view" in {
      val app = application
      val request = FakeRequest(GET, routes.FindAPropertyController.onPageLoad().url).withCSRFToken
      val result = route(app, request).value

      val view = app.injector.instanceOf[FindAPropertyView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form)(request, messages(app)).toString

      app.stop()
    }
  }

  "FindAPropertyController.onSubmit" - {

    "return BAD_REQUEST when form is invalid" in {
      val app = application

      val request =
        FakeRequest(POST, routes.FindAPropertyController.onSubmit().url)
          .withFormUrlEncodedBody("postcode-value" -> "")
          .withCSRFToken

      val result = route(app, request).value
      val view = app.injector.instanceOf[FindAPropertyView]

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(form.bind(Map("postcode-value" -> "")))(request, messages(app)).toString

      app.stop()
    }

    "return error status when connector returns Left(error)" in {
      val app = application

      val error = ErrorResponse(418, "test response")

      when(mockConnector.findAPropertyPostcodeSearch(any())(any()))
        .thenReturn(Future.successful(Left(error)))

      val request =
        FakeRequest(POST, routes.FindAPropertyController.onSubmit().url)
          .withFormUrlEncodedBody("postcode-value" -> "BS14TB")
          .withCSRFToken

      val result = route(app, request).value

      status(result) mustEqual 418

      app.stop()
    }

    "redirect to NoResultsFound when properties list is empty" in {
      val app = application

      val emptyProps = VMVProperties(0, Nil)

      when(mockConnector.findAPropertyPostcodeSearch(any())(any()))
        .thenReturn(Future.successful(Right(emptyProps)))

      when(mockRepo.upsert(any(), any())).thenReturn(Future.successful(true))

      val request =
        FakeRequest(POST, routes.FindAPropertyController.onSubmit().url)
          .withFormUrlEncodedBody("postcode-value" -> "BS153AF")
          .withCSRFToken

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.NoResultsFoundController.onPageLoad.url

      app.stop()
    }

    "redirect to Property search when postcode exist" in {
      val app = application

      val props = VMVProperties(
        total = 1,
        properties = List(
          VMVProperty(1, "1 Test St", "ABC", "REF", Nil)
        )
      )

      when(mockConnector.findAPropertyPostcodeSearch(any())(any()))
        .thenReturn(Future.successful(Right(props)))

      when(mockRepo.upsert(any(), any())).thenReturn(Future.successful(true))

      val request =
        FakeRequest(POST, routes.FindAPropertyController.onSubmit().url)
          .withFormUrlEncodedBody("postcode-value" -> "BH17ST")
          .withCSRFToken

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual ""

      app.stop()
    }
  }
}


