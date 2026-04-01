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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.inject.bind
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import views.html.RatepayerPropertyLinksView

import scala.concurrent.Future

class RatepayerPropertyLinksControllerSpec
  extends SpecBase
    with MockitoSugar {

  private val mockConnector = mock[BridgeIntegrationConnector]

  private def application =
    applicationBuilder(None)
      .overrides(
        bind[BridgeIntegrationConnector].toInstance(mockConnector)
      )
      .build()

  private val personForeignId = "ABC123"
  private val assessmentId = "456"
  

  "RatepayerPropertyLinksController.getRatepayerPropertyLinks" - {

    "must return OK and render the view when connector returns JSON" in {
      val app = application

      val json = Json.obj("foo" -> "bar")

      when(mockConnector.getRatepayerPropertyLinks(any(), any())(any()))
        .thenReturn(Future.successful(json))

      val request =
        FakeRequest(
          GET,
          routes.RatepayerPropertyLinksController
            .getRatepayerPropertyLinks(personForeignId, assessmentId)
            .url
        ).withCSRFToken

      val result = route(app, request).value
      val view = app.injector.instanceOf[RatepayerPropertyLinksView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(json)(request, messages(app)).toString

      app.stop()
    }

    "must return INTERNAL_SERVER_ERROR when connector throws" in {
      val app = application

      when(mockConnector.getRatepayerPropertyLinks(any(), any())(any()))
        .thenReturn(Future.failed(new RuntimeException("test crash")))

      val request =
        FakeRequest(
          GET,
          routes.RatepayerPropertyLinksController
            .getRatepayerPropertyLinks(personForeignId, assessmentId)
            .url
        ).withCSRFToken

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      app.stop()
    }
  }
}
