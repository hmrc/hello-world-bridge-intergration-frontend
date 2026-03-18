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
import connectors.BridgeIntegrationConnector
import models.Status.Approved
import models.components.Card
import models.dashboard.RatepayerStatusResponse
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.DashboardHelper
import views.html.DashboardView
import actions.*

import scala.concurrent.Future

class DashboardControllerSpec extends SpecBase with MockitoSugar {

  private val mockBridgeConnector = mock[BridgeIntegrationConnector]
  private val mockSessionRepository = mock[SessionRepository]

  private val onwardRoute = Call("GET", "/foo")

  def beforeEach(): Unit = {
    reset(mockSessionRepository)
    reset(mockBridgeConnector)

    when(mockSessionRepository.set(any()))
      .thenReturn(Future.successful(true))
  }

  private def buildApp =
    applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(
        bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[BridgeIntegrationConnector].toInstance(mockBridgeConnector),
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
      )
      .build()

  private val request =
    FakeRequest(GET, routes.DashboardController.onPageLoad().url).withCSRFToken

  private def view(app: play.api.Application) =
    app.injector.instanceOf[DashboardView]

  "DashboardController.onPageLoad" - {

    "return 200 and show Registered User when activeRatepayerPersonaExists = true" in {

      when(
        mockBridgeConnector.getDashboard(any())(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(Some(RatepayerStatusResponse(
          activeRatepayerPersonExists = true,
          activeRatepayerPersonaExists = true,
          activePropertyLinkCount = 2
        )))
      )

      val app = buildApp

      val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url).withCSRFToken
      val result = route(app, request).value

      status(result) mustEqual OK
      app.stop()
    }

    "return 303 when a user is not registered" in {

      when(
        mockBridgeConnector.getDashboard(any())(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(Some(RatepayerStatusResponse(
          activeRatepayerPersonExists = false,
          activeRatepayerPersonaExists = false,
          activePropertyLinkCount = 0
        )))
      )

      val app = buildApp

      val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url).withCSRFToken
      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      app.stop()
    }

    "redirect to Journey Recovery when API returns None" in {

      when(mockBridgeConnector.getDashboard(any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(None))

      val app = buildApp

      val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url).withCSRFToken
      val result = route(app, request).value


      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe
        routes.IndexController.onPageLoad().url

      app.stop()
    }

    "return 500 when connector throws an unexpected exception" in {

      when(mockBridgeConnector.getDashboard(any())(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val app = buildApp

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe
        routes.IndexController.onPageLoad().url

      app.stop()
    }
  }
}