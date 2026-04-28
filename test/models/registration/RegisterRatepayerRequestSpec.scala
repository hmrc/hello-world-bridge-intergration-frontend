/*
 * Copyright 2025 HM Revenue & Customs
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

package models.registration

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsSuccess, Json}

  class RegisterRatepayerRequestSpec extends PlaySpec {
    "RatepayerRegistration JSON format" should {
      "serialize and deserialize correctly with all fields present" in {
        val model = RegisterRatepayer(
          userType = Some(RatepayerType.Individual),
          agentStatus = Some(AgentStatus.Agent),
          name = Some("Test Name"),
          tradingName = Some(TradingName("Trading Name")),
          email = Some("test@email.com"),
          nino = Some(Nino("AA123456A")),
          contactNumber = Some("0123456789"),
          secondaryNumber = Some("0987654321"),
          address = Some("1 Test St, Suite 2, Testville, Testshire TE5 7ST"),
          trnReferenceNumber = Some(TRNReferenceNumber(ReferenceType.Trn, "TRN-123")),
          isRegistered = Some(true),
          recoveryId = Some("1234567890")
        )
        val json = Json.toJson(model)
        json.as[RegisterRatepayer] mustEqual model
      }

      "serialize and deserialize correctly with only required fields (all None except isRegistered)" in {
        val model = RegisterRatepayer()
        val json = Json.toJson(model)
        json.as[RegisterRatepayer] mustEqual model
      }

      "deserialize from JSON with missing optional fields" in {
        val json = Json.parse(
          """
          {
            "isRegistered": false
          }
          """
        )
        json.validate[RegisterRatepayer] mustEqual JsSuccess(RegisterRatepayer(isRegistered = Some(false)))
      }
    }
  }
