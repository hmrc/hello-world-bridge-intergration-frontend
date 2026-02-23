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

import Connector.BridgeIntegrationConnector
import mocks.MockHttpV2
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.client.HttpClientV2


class BridgeIntegrationConnectorSpec extends MockHttpV2
  with GuiceOneAppPerSuite {

  override lazy val app = new GuiceApplicationBuilder()
    .overrides(
      api.inject.bind[HttpClientV2].toInstance(mockHttpClientV2)
    )
    .configure("bridgeIntegration" -> "http://localhost:1300")
    .build()

  val connector: BridgeIntegrationConnector = app.injector.instanceOf[BridgeIntegrationConnector]

  "BridgeIntegrationConnector.registerRatePayer" should {

    "return true when ACCEPTED is returned" in {
      setupMockHttpV2Post(
        "http://localhost:1300/bridge-integration/register-ratepayer/123456789123"
      )(
        HttpResponse(ACCEPTED, Json.obj(), Map.empty)
      )

      connector.registerRatePayer(testRegistrationModel).futureValue mustBe true
    }

    "return false when NOT_ACCEPTABLE (406) is returned" in {
      setupMockHttpV2Post(
        "http://localhost:1300/bridge-integration/register-ratepayer/123456789123"
      )(
        HttpResponse(NOT_ACCEPTABLE, Json.obj(), Map.empty)
      )

      connector.registerRatePayer(testRegistrationModel).futureValue mustBe false
    }

    "return false when INTERNAL_SERVER_ERROR (500) is returned" in {
      setupMockHttpV2Post(
        "http://localhost:1300/bridge-integration/register-ratepayer/123456789123"
      )(
        HttpResponse(INTERNAL_SERVER_ERROR, Json.obj(), Map.empty)
      )

      connector.registerRatePayer(testRegistrationModel).futureValue mustBe false
    }

    "return false when an exception is thrown" in {
      setupMockHttpV2FailedPost(
        "http://localhost:1300/bridge-integration/register-ratepayer/123456789123"
      )

      connector.registerRatePayer(testRegistrationModel).futureValue mustBe false
    }
  }
}
