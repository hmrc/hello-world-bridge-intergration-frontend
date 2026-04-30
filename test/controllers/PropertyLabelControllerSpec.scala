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
import forms.PropertyLabelFormProvider
import models.UserAnswers
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.property.PropertyLabelPage
import play.api.data.FormBinding.Implicits.formBinding
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.property.PropertyLabelView

import scala.concurrent.Future

class PropertyLabelControllerSpec
  extends SpecBase
    with MockitoSugar
    with ScalaFutures
    with BeforeAndAfterEach {

  private val mockSessionRepository = mock[SessionRepository]
  private val mockNavigator         = mock[Navigator]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository, mockNavigator)
  }

  private def applicationWithUserAnswers(userAnswers: UserAnswers) =
    applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[Navigator].toInstance(mockNavigator)
      )
      .build()

  "PropertyLabelController onPageLoad" - {

    "return OK and render the view when no value is present" in {

      val userAnswers = emptyUserAnswers

      val app = applicationWithUserAnswers(userAnswers)
      val view = app.injector.instanceOf[PropertyLabelView]

      val request = FakeRequest(GET, routes.PropertyLabelController.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe view(
        new PropertyLabelFormProvider()()
      )(request, messages(app)).toString

      app.stop()
    }

    "populate the form when a previously entered value exists" in {

      val userAnswers =
        emptyUserAnswers.set(PropertyLabelPage, "Test property label").success.value

      val app = applicationWithUserAnswers(userAnswers)
      val view = app.injector.instanceOf[PropertyLabelView]

      val request = FakeRequest(GET, routes.PropertyLabelController.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustBe OK
      contentAsString(result) mustBe view(
        new PropertyLabelFormProvider()().fill("Test property label")
      )(request, messages(app)).toString

      app.stop()
    }
  }

  "PropertyLabelController onSubmit" - {

    "return BadRequest and render errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers

      val app = applicationWithUserAnswers(userAnswers)
      val view = app.injector.instanceOf[PropertyLabelView]

      val request =
        FakeRequest(POST, routes.PropertyLabelController.onSubmit().url)
          .withFormUrlEncodedBody("value" -> "")

      val result = route(app, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe view(
        new PropertyLabelFormProvider()().bindFromRequest()(request)
      )(request, messages(app)).toString

      app.stop()
    }

    "redirect to Check Your Answers and persist data when valid data is submitted" in {

      val userAnswers = emptyUserAnswers

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val app = applicationWithUserAnswers(userAnswers)

      val request =
        FakeRequest(POST, routes.PropertyLabelController.onSubmit().url)
          .withFormUrlEncodedBody("value" -> "My Property Label")

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe
        routes.CheckYourAnswersPropertyAssessmentController.onPageLoad().url

      verify(mockSessionRepository, times(1)).set(any())

      app.stop()
    }
  }
}