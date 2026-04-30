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
import forms.PropertyAddressFormProvider
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.property.PropertyAddressPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._

import repositories.SessionRepository
import views.html.property.PropertyAddressView

import scala.concurrent.Future

class PropertyAddressControllerSpec
  extends SpecBase
    with MockitoSugar {

  private val formProvider = new PropertyAddressFormProvider
  private val form         = formProvider()

  private val mockSessionRepository = mock[SessionRepository]

  private val addressValue = "123 Test Street"

  private def application(userAnswers: Option[UserAnswers]) =
    applicationBuilder(userAnswers = userAnswers)
      .overrides(
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
      .build()

  "PropertyAddressController onPageLoad" - {

    "return OK and the correct view with an empty form when no answer is present" in {

      val app = application(Some(emptyUserAnswers))
      val request = FakeRequest(GET, routes.PropertyAddressController.onPageLoad().url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[PropertyAddressView]

      status(result) mustBe OK
      contentAsString(result) mustBe
        view(form)(request, messages(app)).toString
    }

    "return OK and the correct view with a populated form when an answer exists" in {

      val userAnswers =
        emptyUserAnswers
          .set(PropertyAddressPage, addressValue)
          .success
          .value

      val app = application(Some(userAnswers))
      val request = FakeRequest(GET, routes.PropertyAddressController.onPageLoad().url)

      val result = route(app, request).value

      val view = app.injector.instanceOf[PropertyAddressView]

      status(result) mustBe OK
      contentAsString(result) mustBe
        view(form.fill(addressValue))(request, messages(app)).toString
    }
  }

  "PropertyAddressController onSubmit" - {

    "redirect to Check Your Answers page when valid data is submitted" in {

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val app = application(Some(emptyUserAnswers))

      val request =
        FakeRequest(POST, routes.PropertyAddressController.onSubmit().url)
          .withFormUrlEncodedBody("value" -> addressValue)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe
        routes.CheckYourAnswersPropertyAssessmentController.onPageLoad().url

      verify(mockSessionRepository).set(any())
    }

    "return BadRequest and re-render the view when submitted data is invalid" in {

      val app = application(Some(emptyUserAnswers))

      val request =
        FakeRequest(POST, routes.PropertyAddressController.onSubmit().url)
          .withFormUrlEncodedBody("value" -> "") // invalid

      val result = route(app, request).value

      val view = app.injector.instanceOf[PropertyAddressView]

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe
        view(form.bind(Map("value" -> "")))(request, messages(app)).toString
    }
  }
}
