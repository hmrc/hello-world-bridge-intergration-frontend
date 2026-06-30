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

package controllers.relationship

import base.SpecBase
import controllers.routes
import forms.RelationshipLabelFormProvider
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.relationship.RelationshipLabelPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.relationship.RelationshipLabelView

import scala.concurrent.Future

class RelationshipLabelControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new RelationshipLabelFormProvider()
  private val form         = formProvider()

  private val validAnswer = "Associated Company"
  private val mockSessionRepository = mock[SessionRepository]

  when(mockSessionRepository.set(any()))
    .thenReturn(Future.successful(true))

  private val onwardRoute = Call("GET", "/foo")

  lazy val app =
    applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
      .build()

  "RelationshipLabelController onPageLoad" - {
    "return OK and render the view" in {
      val request =
        FakeRequest(GET, routes.RelationshipLabelController.onPageLoad().url)
      val result = route(app, request).value
      val view = app.injector.instanceOf[RelationshipLabelView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual
        view(form)(request, messages(app)).toString
    }

    "populate the form when an existing answer is available" in {
      val userAnswers =
        emptyUserAnswers.set(RelationshipLabelPage, validAnswer).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
      running(application) {

        val request =
          FakeRequest(GET, routes.RelationshipLabelController.onPageLoad().url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[RelationshipLabelView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form.fill(validAnswer))(request, messages(application)).toString
      }
    }
  }
  
  "RelationshipLabelController onSubmit" - {
    "redirect to Check Your Answers when valid data is submitted" in {
      val request =
        FakeRequest(POST, routes.RelationshipLabelController.onSubmit().url).withFormUrlEncodedBody("value" -> validAnswer)
      val result = route(app, request).value
      status(result) mustEqual SEE_OTHER
      
      redirectLocation(result).value mustEqual routes.CheckYourAnswersRatepayerPropertyLinksController.onPageLoad().url
      
      verify(mockSessionRepository).set(any())
    }
    
    "return BAD_REQUEST when invalid data is submitted" in {
      val request =
        FakeRequest(POST, routes.RelationshipLabelController.onSubmit().url).withFormUrlEncodedBody("value" -> "")
      val result = route(app, request).value
      status(result) mustEqual BAD_REQUEST
    }
  }
}

 