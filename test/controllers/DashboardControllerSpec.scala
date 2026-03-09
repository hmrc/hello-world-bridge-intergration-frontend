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
import connector.BridgeIntegrationConnector
import models.Status.{Approved, Pending}
import models.components.Card
import models.dashboard.RatepayerStatusResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.{Application, inject}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.DashboardHelper
import views.html.DashboardView

import scala.concurrent.Future

class DashboardControllerSpec extends SpecBase {

  private val mockBridgeConnector = mock[BridgeIntegrationConnector]

  "DashboardController" - {

      "must return OK and the correct view for when a user is registered and has properties a GET" in {

        val cards: Application => Seq[Card] = app => DashboardHelper.getDashboardCards(true, Approved)(messages(app: Application))
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[DashboardView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(cards(application),"Registered User")(request, messages(application)).toString
        }

      }
      "must return OK and the correct view for when a user is not registered GET" in {

        val cards: Application => Seq[Card] = app => DashboardHelper.getDashboardCards(false, Pending)(messages(app: Application))

        when(mockBridgeConnector.getDashboard(any())(any()))
          .thenReturn(Future.successful(Some(RatepayerStatusResponse(false, false, 0))))

        val application = applicationBuilder(userAnswers = None)
          .overrides(inject.bind[BridgeIntegrationConnector].toInstance(mockBridgeConnector))
          .build()

        running(application) {

          val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[DashboardView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(cards(application), "Non Registered User")(request, messages(application)).toString
        }
      }
    "must return OK and the correct view for when the call to the bridge returns none GET" in {

      val cards: Application => Seq[Card] = app => DashboardHelper.getDashboardCards(false, Pending)(messages(app: Application))

      when(mockBridgeConnector.getDashboard(any())(any()))
        .thenReturn(Future.successful(None))

      val application = applicationBuilder(userAnswers = None)
        .overrides(inject.bind[BridgeIntegrationConnector].toInstance(mockBridgeConnector))
        .build()

      running(application) {

        val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DashboardView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(cards(application), "Non Registered User")(request, messages(application)).toString
      }
    }
    "must return OK and the correct view for when the call to the bridge fails GET" in {

      val cards: Application => Seq[Card] = app => DashboardHelper.getDashboardCards(false, Pending)(messages(app: Application))

      when(mockBridgeConnector.getDashboard(any())(any()))
        .thenReturn(Future.failed(new Exception("bridge failure")))

      val application = applicationBuilder(userAnswers = None)
        .overrides(inject.bind[BridgeIntegrationConnector].toInstance(mockBridgeConnector))
        .build()

      running(application) {

        val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DashboardView]

        status(result) mustEqual SEE_OTHER
      }
    }
    }
    
  }
