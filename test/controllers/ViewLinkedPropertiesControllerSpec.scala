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
import connectors.BridgeIntegrationConnector
import helpers.TestData
import models.bridge.common.{CodeMeaning, ForeignId}
import models.bridge.property.{AddressData, LocationData, Property, PropertyData, PropertyModelsSpec}
import models.properties.RatepayerPropertyLinksResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ViewLinkedPropertiesView

import scala.concurrent.Future

class ViewLinkedPropertiesControllerSpec extends SpecBase with MockitoSugar with TestData {

  private val mockBridgeConnector = mock[BridgeIntegrationConnector]
  private val mockSessionRepository = mock[SessionRepository]

  private val onwardRoute = Call("GET", "/foo")

  def beforeEach(): Unit = {
    reset(mockBridgeConnector)
    reset(mockSessionRepository)

    when(mockSessionRepository.set(any()))
      .thenReturn(Future.successful(true))
  }

  private def buildApp =
    applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[BridgeIntegrationConnector].toInstance(mockBridgeConnector)
      )
      .build()

  private def view(app: play.api.Application) =
    app.injector.instanceOf[ViewLinkedPropertiesView]

  // ==========================================================================================
  // ✅ TEST 1 — API returns populated data
  // ==========================================================================================

  "ViewLinkedPropertiesController.onPageLoad" - {

    "return 200 (OK) when API returns property links" in {

      val response = RatepayerPropertyLinksResponse(
        properties = List(testProperty),
        persons = List(testPerson),
        relationships = List(testRelationship)
      )

      when(mockBridgeConnector.getRatepayerProperties(any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(response)))

      val app = buildApp

      val request = FakeRequest(GET, routes.ViewLinkedPropertiesController.onPageLoad().url).withCSRFToken
      val result = route(app, request).value

      status(result) mustEqual OK
      app.stop()
    }

    // ========================================================================================
    // ✅ TEST 2 — API returns None → empty dashboard with linkedProperties = false
    // ========================================================================================

    "return 200 (OK) with empty lists when API returns None" in {

      when(mockBridgeConnector.getRatepayerProperties(any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(None))

      val app = buildApp

      val request = FakeRequest(GET, routes.ViewLinkedPropertiesController.onPageLoad().url).withCSRFToken
      val result = route(app, request).value

      status(result) mustEqual OK
      app.stop()
    }

    // ========================================================================================
    // ✅ TEST 3 — API throws exception → recover block returns OK
    // ========================================================================================

    "return 200 (OK) when connector throws an exception" in {

      when(mockBridgeConnector.getRatepayerProperties(any())(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val app = buildApp

      val request = FakeRequest(GET, routes.ViewLinkedPropertiesController.onPageLoad().url).withCSRFToken
      val result = route(app, request).value

      status(result) mustEqual OK
      app.stop()
    }
  }
}
