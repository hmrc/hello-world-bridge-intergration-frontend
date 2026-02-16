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

package controllers

import base.SpecBase
import forms.ContactNumberFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.ContactNumberPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.ContactNumberView

import scala.concurrent.Future

class ContactNumberControllerSpec
  extends SpecBase
    with MockitoSugar {

  private val formProvider = new ContactNumberFormProvider()
  private val form = formProvider()

  private val onwardRoute = Call("GET", "/foo")

  private val mockSessionRepository = mock[SessionRepository]

  def beforeEach(): Unit = {
    reset(mockSessionRepository)
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
  }

  private def applicationWithAnswers(userAnswers: Option[UserAnswers]) =
    applicationBuilder(userAnswers)
      .overrides(
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
      .build()

  "ContactNumberController" - {

    "onPageLoad" - {

      "must return OK and the correct view with empty form when no existing answer" in {
        val application = applicationWithAnswers(Some(emptyUserAnswers))
        val request = FakeRequest(GET, routes.ContactNumberController.onPageLoad(NormalMode).url).withCSRFToken
        val result = route(application, request).value
        val view = application.injector.instanceOf[ContactNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString

        application.stop()
      }

      "must return OK and pre-populate form when answer exists" in {
        val userAnswers = emptyUserAnswers.set(ContactNumberPage, "07777777777".toInt).success.value
        val application = applicationWithAnswers(Some(userAnswers))

        val request = FakeRequest(GET, routes.ContactNumberController.onPageLoad(NormalMode).url).withCSRFToken
        val result = route(application, request).value
        val view = application.injector.instanceOf[ContactNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("07777777777".toInt), NormalMode)(request, messages(application)).toString

        application.stop()
      }
    }

    "onSubmit" - {

      "must save the answer and redirect on valid submission" in {
        val application = applicationWithAnswers(Some(emptyUserAnswers))

        val request =
          FakeRequest(POST, routes.ContactNumberController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody("value" -> "07777777777")
            .withCSRFToken

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        // Ensure repository was called with updated answers
        verify(mockSessionRepository, times(1)).set(any())

        application.stop()
      }

      "must return bad request and display errors on invalid submission" in {
        val application = applicationWithAnswers(Some(emptyUserAnswers))

        val request =
          FakeRequest(POST, routes.ContactNumberController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody("value" -> "") // invalid
            .withCSRFToken

        val result = route(application, request).value
        val view = application.injector.instanceOf[ContactNumberView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(form.withError("value", "contactNumber.error.required"), NormalMode)(request, messages(application)).toString

        application.stop()
      }
    }
  }
}
