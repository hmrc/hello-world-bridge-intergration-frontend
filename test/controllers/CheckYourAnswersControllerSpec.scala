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
import forms.{ContactNumberFormProvider, UserNameFormProvider}
import helpers.ControllerSpecSupport
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.UserNamePage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.test.Helpers.fakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.NotFoundException
import viewmodels.govuk.SummaryListFluency
import views.html.{CheckYourAnswersView, UserNameView}

import java.time.Instant
import scala.concurrent.Future

class CheckAnswersControllerSpec extends SpecBase with MockitoSugar {

  private val onwardRoute = Call("GET", "/foo")
  private val pageTitle = "Check your answers"

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

  "CheckAnswersController" - {
    "onPageLoad" - {
      "must return OK and the correct view with empty form when no existing answer" in {
        val application = applicationWithAnswers(Some(UserAnswers(
          "1234", Json.obj(
            "contactNumber" ->  12345,
            "userName" -> "Jake"
          ), Instant.now)))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url).withCSRFToken
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) must include(pageTitle)
        contentAsString(result) must include ("Jake")
        contentAsString(result) must include ("12345")

        application.stop()
      }
    }
  }
}
