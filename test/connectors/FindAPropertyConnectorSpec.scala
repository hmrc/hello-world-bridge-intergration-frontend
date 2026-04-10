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

package connectors

import scala.concurrent.{ExecutionContext, Future}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import models.*
import forms.FindAPropertyForm
import models.registration.Postcode
import org.mockito.ArgumentMatchers.any
import org.scalatest.matchers.should.Matchers
import mocks.MockAppConfig
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures



class FindAPropertyConnectorSpec
  extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures
    with EitherValues
    with MockAppConfig {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private def mockRequestReturning(response: HttpResponse): HttpClientV2 = {
    val http = mock[HttpClientV2]
    val builder = mock[RequestBuilder]

    when(http.get(any())(any())).thenReturn(builder)
    when(builder.execute[HttpResponse](any(), any()))
      .thenReturn(Future.successful(response))

    http
  }

  private val sampleJson =
    Json.obj(
      "total" -> 1,
      "properties" -> Json.arr(
        Json.obj(
          "uarn" -> 123L,
          "addressFull" -> "1 Test Street",
          "localAuthorityCode" -> "ABC",
          "localAuthorityReference" -> "REF1",
          "valuations" -> Json.arr()
        )
      ),
      "hasNext" -> false,
      "hasPrevious" -> false
    )

  private val sampleForm =
    FindAPropertyForm(Postcode("BH1 7ST"), None)

  "FindAPropertyConnector" should {

    "return Right(VMVProperties) when status is 200 and JSON is valid" in {
      val http = mockRequestReturning(
        HttpResponse(200, sampleJson, Map.empty)
      )

      val connector = new FindAPropertyConnector(http, mockConfig)

      val result = connector.findAPropertyPostcodeSearch(sampleForm).futureValue

      result.isRight shouldBe true
      result.toOption.get.total shouldBe 1
    }

    "return Right(VMVProperties) when status is 404 and JSON is valid" in {
      val http = mockRequestReturning(
        HttpResponse(404, sampleJson, Map.empty)
      )

      val connector = new FindAPropertyConnector(http, mockConfig)

      val result = connector.findAPropertyPostcodeSearch(sampleForm).futureValue

      result.isRight shouldBe true
    }

    "return Left(ErrorResponse) when JSON is invalid" in {
      val http = mockRequestReturning(
        HttpResponse(200, Json.obj("invalid" -> "json"), Map.empty)
      )

      val connector = new FindAPropertyConnector(http, mockConfig)

      val result = connector.findAPropertyPostcodeSearch(sampleForm).futureValue

      result.isLeft shouldBe true
      result.left.value.statusCode shouldBe 400
    }

    "return Left(ErrorResponse) for non-OK status" in {
      val http = mockRequestReturning(
        HttpResponse(500, "")
      )

      val connector = new FindAPropertyConnector(http, mockConfig)

      val result = connector.findAPropertyPostcodeSearch(sampleForm).futureValue

      result.isLeft shouldBe true
      result.left.value.statusCode shouldBe 500
    }

    "return Left(ErrorResponse) when an exception occurs" in {
      val http = mock[HttpClientV2]
      val builder = mock[RequestBuilder]

      when(http.get(any())(any())).thenReturn(builder)
      when(builder.execute[HttpResponse](any(), any()))
        .thenReturn(Future.failed(new RuntimeException("fail")))

      val connector = new FindAPropertyConnector(http, mockConfig)

      val result = connector.findAPropertyPostcodeSearch(sampleForm).futureValue

      result.isLeft shouldBe true
      result.left.value.statusCode shouldBe 500
    }
  }
}