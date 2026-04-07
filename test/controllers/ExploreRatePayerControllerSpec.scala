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
import helpers.TestData
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.test.CSRFTokenHelper.CSRFRequest
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ExploreRatepayerView

import scala.concurrent.Future

class ExploreRatePayerControllerSpec extends SpecBase with MockitoSugar with TestData {

  private val mockBridgeConnector = mock[BridgeIntegrationConnector]

  def beforeEach(): Unit = {
    reset(mockBridgeConnector)
  }

  private def buildApp =
    applicationBuilder(None)
      .overrides(
        bind[BridgeIntegrationConnector].toInstance(mockBridgeConnector)
      )
      .build()

  private val request =
    FakeRequest(GET, routes.ExploreRatePayerController.exploreRatePayer().url).withCSRFToken

  private def view(app: play.api.Application) =
    app.injector.instanceOf[ExploreRatepayerView]

  "ExploreRatePayerController.exploreRatePayer" - {

    "return 200 and show a ratepayer as a summary list" in {

      when(
        mockBridgeConnector.exploreRatePayer(any())(any[HeaderCarrier])
      ).thenReturn(
        Future.successful(Some(personsDataMax))
      )

      val app = buildApp

      val request = FakeRequest(GET, routes.ExploreRatePayerController.exploreRatePayer().url).withCSRFToken
      val result = route(app, request).value

      status(result) mustEqual OK
      app.stop()
    }
    "return an Exception when the ratepayer is not found" in {
      when(mockBridgeConnector.exploreRatePayer(any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(None))

      val app = buildApp


      val exception = intercept[Exception] {
        val result = route(app, request).value
        await(result)

      }

      exception.getMessage mustEqual "Failed to retrieve person"
    }
  }
}
