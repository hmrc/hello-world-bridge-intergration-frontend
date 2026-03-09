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

import base.SpecBase
import connector.BridgeIntegrationConnector
import controllers.routes
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.{ContactNumberPage, UserNamePage}
import play.api.inject.bind
import play.api.test.CSRFTokenHelper.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec
  extends SpecBase
    with MockitoSugar {

  private val mockSessionRepository = mock[SessionRepository]
  private val mockBridgeConnector   = mock[BridgeIntegrationConnector]

  private def applicationWithAnswers(answers: Option[UserAnswers]) =
    applicationBuilder(answers)
      .overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[BridgeIntegrationConnector].toInstance(mockBridgeConnector)
      )
      .build()

  def beforeEach(): Unit = {
    reset(mockSessionRepository, mockBridgeConnector)
    when(mockSessionRepository.get(any()))
      .thenReturn(Future.successful(Some(emptyUserAnswers)))
  }

  "CheckYourAnswersController" - {

    "onPageLoad" - {

      "must return OK and render the CheckYourAnswersView" in {
        val answers = emptyUserAnswers
          .set(UserNamePage, "John Doe").success.value
          .set(ContactNumberPage, 777777777).success.value

        val application = applicationWithAnswers(Some(answers))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
          .withCSRFToken

        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          service.CheckAnswers.createSummaryRows(answers)(messages(application))
        )(request, messages(application)).toString

        application.stop()
      }
    }

    "onSubmit" - {

      "must call the bridge connector and redirect to Dashboard on success" in {
        // Mock bridge connector success
        when(mockBridgeConnector.registerRatePayer(any())(any()))
          .thenReturn(Future.successful(true))

        val answers = emptyUserAnswers
          .set(UserNamePage, "John Doe").success.value
          .set(ContactNumberPage, 777777777).success.value

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(answers)))

        val app = applicationWithAnswers(Some(answers))
        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
          .withCSRFToken

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.DashboardController.onPageLoad().url

        verify(mockBridgeConnector, times(1)).registerRatePayer(any())(any())
        app.stop()
      }

      "must redirect to IndexController if no session data found" in {
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(None))

        val app = applicationWithAnswers(Some(emptyUserAnswers))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
          .withCSRFToken

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url

        app.stop()
      }

      "must redirect to IndexController when bridge submission fails" in {
        when(mockBridgeConnector.registerRatePayer(any())(any()))
          .thenReturn(Future.failed(new Exception("bridge failure")))

        val answers = emptyUserAnswers

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(answers)))

        val app = applicationWithAnswers(Some(answers))
        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
          .withCSRFToken

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url

        app.stop()
      }
    }
  }
}