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
import forms.UserNameFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.UserNamePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.UserNameView

import scala.concurrent.Future

class UserNameControllerSpec
  extends SpecBase
    with MockitoSugar {

  private val formProvider = new UserNameFormProvider()
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

  "UserNameController" - {

    "onPageLoad" - {

      "must return OK and the correct view with empty form when no existing answer" in {
        val application = applicationWithAnswers(Some(emptyUserAnswers))
        val request = FakeRequest(GET, routes.UserNameController.onPageLoad(NormalMode).url).withCSRFToken
        val result = route(application, request).value
        val view = application.injector.instanceOf[UserNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString

        application.stop()
      }

      "must return OK and pre-populate form when answer exists" in {
        val userAnswers = emptyUserAnswers.set(UserNamePage, "Jake").success.value
        val application = applicationWithAnswers(Some(userAnswers))

        val request = FakeRequest(GET, routes.UserNameController.onPageLoad(NormalMode).url).withCSRFToken
        val result = route(application, request).value
        val view = application.injector.instanceOf[UserNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill( "Jake"), NormalMode)(request, messages(application)).toString

        application.stop()
      }
    }

    "onSubmit" - {
      "must save the answer and redirect on valid submission" in {
        val app = applicationWithAnswers(Some(emptyUserAnswers))
        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true)) // ensure not null

        val request =
          FakeRequest(POST, routes.ContactNumberController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody("value" -> "777735677")
            .withCSRFToken

        val result = route(app, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockSessionRepository, times(1)).set(any())
        app.stop()
      }


      "must return bad request and display errors on invalid submission" in {
        val application = applicationWithAnswers(Some(emptyUserAnswers))

        val request =
          FakeRequest(POST, routes.UserNameController.onSubmit(NormalMode).url)
            .withFormUrlEncodedBody("value" -> "") // invalid
            .withCSRFToken

        val result = route(application, request).value
        val view = application.injector.instanceOf[UserNameView]

        status(result) mustEqual BAD_REQUEST

        val boundForm = form.bind(Map("value" -> ""))
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString

        application.stop()
      }
    }
  }
}

