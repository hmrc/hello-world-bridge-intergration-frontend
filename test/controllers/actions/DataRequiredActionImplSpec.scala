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

package controllers.actions

import controllers.routes
import models.UserAnswers
import models.requests.{DataRequest, OptionalDataRequest}

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.mvc.{AnyContent, Request, Result}
import play.api.mvc.Results.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionImplSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures {

  implicit val ec: ExecutionContext = ExecutionContext.global

  private val action = new DataRequiredActionImpl()

  private val baseRequest: Request[AnyContent] =
    FakeRequest(GET, "/test-url")

  "DataRequiredActionImpl" should {

    "redirect to JourneyRecoveryController when userAnswers is None" in {

      val request = OptionalDataRequest(
        request = baseRequest,
        userId = "user-id-123",
        userAnswers = None
      )

      val result: Result =
        action.invokeBlock(request, (_: DataRequest[AnyContent]) =>
          fail("Block should not be called when userAnswers is None")
        ).futureValue

      result.header.status mustBe SEE_OTHER
      result.header.headers(LOCATION) mustBe
        routes.JourneyRecoveryController.onPageLoad().url
    }

    "call the block with a DataRequest when userAnswers is present" in {

      val userAnswers = UserAnswers("user-id-123")

      val request = OptionalDataRequest(
        request = baseRequest,
        userId = "user-id-123",
        userAnswers = Some(userAnswers)
      )

      val result =
        action.invokeBlock(request, (dataRequest: DataRequest[AnyContent]) => {
          dataRequest.userId mustBe "user-id-123"
          dataRequest.userAnswers mustBe userAnswers
          dataRequest.request mustBe baseRequest

          Future.successful(Ok) // ✅ Result, not Int
        }).futureValue

      result.header.status mustBe OK
    }
  }
}