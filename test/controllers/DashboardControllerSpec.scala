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
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.DashboardHelper
import views.html.DashboardView

import scala.concurrent.Future

class DashboardControllerSpec extends SpecBase with MockitoSugar{

  private val mockBridgeConnector = mock[BridgeIntegrationConnector]

  private val onwardRoute = Call("GET", "/foo")

  private val mockSessionRepository = mock[SessionRepository]

  def beforeEach(): Unit = {
    reset(mockSessionRepository)
    when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
  }

  private def buildApp =
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()


  private def view(app: play.api.Application) =
    app.injector.instanceOf[DashboardView]

  private val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url)

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
      val expectedCards: Seq[Card] =
        DashboardHelper.getDashboardCards(isPropertyLinked = true, status = Approved)(messages(app))

      val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url).withCSRFToken
      val result = route(app, request).value
      val view = app.injector.instanceOf[DashboardView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(expectedCards, "Registered User")(request, messages(app)).toString

      app.stop()
    }
  }
}