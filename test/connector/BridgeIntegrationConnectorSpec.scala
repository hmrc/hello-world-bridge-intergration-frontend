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
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.Fault
import config.FrontendAppConfig
import helpers.{IntegrationSpecBase, WiremockHelper}
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.test.Injecting
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class BridgeIntegrationConnectorSpec extends AnyWordSpec with IntegrationSpecBase with Injecting {


  lazy val connector: BridgeIntegrationConnector = app.injector.instanceOf[BridgeIntegrationConnector]


  override def beforeEach(): Unit = {
    WireMock.reset()
  }

  "NgrNotifyConnector" when {

    "calling .isAllowedInPrivateBeta()" should {
      "return true when allowed is true in response" in {
        val credId = "test-cred-id"
        val responseJson = Json.obj("allowed" -> true)

        WiremockHelper.stubGet(s"/bridge-integration/allowed-in-private-beta/$credId", OK, responseJson.toString())

        val result = connector.isAllowedInPrivateBeta(credId).futureValue
        result mustBe true

        WiremockHelper.verifyGet(s"/bridge-integration/allowed-in-private-beta/$credId")
      }

      "return false when allowed is false in response" in {
        val credId = "test-cred-id"
        val responseJson = Json.obj("allowed" -> false)

        WiremockHelper.stubGet(s"/bridge-integration/allowed-in-private-beta/$credId", OK, responseJson.toString())

        val result = connector.isAllowedInPrivateBeta(credId).futureValue
        result mustBe false

        WiremockHelper.verifyGet(s"/bridge-integration/allowed-in-private-beta/$credId")
      }

      "return false when response is not OK" in {
        val credId = "test-cred-id"

        WiremockHelper.stubGet(s"/bridge-integration/allowed-in-private-beta/$credId", INTERNAL_SERVER_ERROR, "error")

        val result = connector.isAllowedInPrivateBeta(credId).futureValue
        result mustBe false

        WiremockHelper.verifyGet(s"/bridge-integration/allowed-in-private-beta/$credId")
      }
    }

    "calling .registerRatePayer()" should {
      "return ACCEPTED when registration is successful" in {
        WiremockHelper.stubPost(
          "/bridge-integration/register-ratepayer/123456789123",
          ACCEPTED,
          """{"status": "OK"}"""
        )

        val result = connector.registerRatePayer(sampleRatepayerRegistration).futureValue
        result mustBe true

        WiremockHelper.verifyPost("/bridge-integration/register-ratepayer/123456789123", Some(Json.toJson(sampleRatepayerRegistration).toString()))
      }

      val clientErrorCodes = Seq(400, 401, 403, 404, 405, 409, 410, 415, 422, 429)

      clientErrorCodes.foreach { statusCode =>
        s"return $statusCode response without throwing for client error" in {
          WiremockHelper.stubPost(
            "/bridge-integration/register-ratepayer/123456789123",
            statusCode,
            s"""{"status": "$statusCode", "error": "Client error"}"""
          )

          val result = connector.registerRatePayer(sampleRatepayerRegistration).futureValue
          result mustBe false

          WiremockHelper.verifyPost("/bridge-integration/register-ratepayer/123456789123", Some(Json.toJson(sampleRatepayerRegistration).toString()))
        }
      }

      "throw an exception for $statusCode server error" in {
        WiremockHelper.stubWithFault(
          "POST",
          "/bridge-integration/register-ratepayer/123456789123",
          Fault.CONNECTION_RESET_BY_PEER
        )

        val result = connector.registerRatePayer(sampleRatepayerRegistration).futureValue
        result mustBe false

        WiremockHelper.verifyPost("/bridge-integration/register-ratepayer/123456789123", Some(Json.toJson(sampleRatepayerRegistration).toString()))
      }
    }
  }
}
