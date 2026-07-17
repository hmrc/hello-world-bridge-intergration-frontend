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
import forms.RelationshipDescriptionFormProvider
import models.UserAnswers
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.RelationshipDescriptionPage
import play.api.Application
import play.api.inject
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.relationship.RelationshipDescriptionView

import scala.concurrent.Future

class RelationshipDescriptionControllerSpec
  extends SpecBase
    with MockitoSugar
    with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]

  private val formProvider = new RelationshipDescriptionFormProvider()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
  }

  private val onwardRoute = Call("GET", "/foo")

  private def applicationWithAnswers(userAnswers: Option[UserAnswers]) =
    applicationBuilder(userAnswers)
      .overrides(
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
      .build()

  "onPageLoad" - {

    "return OK and render the view for a GET" in {

      val application = applicationWithAnswers(Some(emptyUserAnswers))

      val request =
        FakeRequest(GET, routes.RelationshipDescriptionController.onPageLoad().url)
          .withCSRFToken

      val result = route(application, request).value

      val view =
        application.injector.instanceOf[RelationshipDescriptionView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(formProvider())(request, messages(application)).toString

      application.stop()
    }
  }

  "onSubmit" - {

    "redirect to CheckYourAnswersRatepayerPropertyLinksController when valid data submitted" in {

      val application = applicationWithAnswers(Some(emptyUserAnswers))

      val request =
        FakeRequest(POST, routes.RelationshipDescriptionController.onSubmit().url)
          .withFormUrlEncodedBody(
            "value" -> "My relationship description"
          )
          .withCSRFToken

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        routes.CheckYourAnswersRatepayerPropertyLinksController.onPageLoad().url

      verify(mockSessionRepository).set(any())

      application.stop()
    }

    "return BAD_REQUEST when invalid data submitted" in {

      val application = applicationWithAnswers(Some(emptyUserAnswers))

      val request =
        FakeRequest(POST, routes.RelationshipDescriptionController.onSubmit().url)
          .withFormUrlEncodedBody(
            "value" -> ""
          )
          .withCSRFToken

      val result = route(application, request).value

      val view =
        application.injector.instanceOf[RelationshipDescriptionView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(
          formProvider().bind(
            Map("value" -> "")
          )
        )(request, messages(application)).toString

      application.stop()
    }
  }
}