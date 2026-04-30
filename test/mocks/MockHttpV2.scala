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

package mocks

import helpers.TestSupport
import helpers.WiremockHelper.url
import models.properties.RatepayerPropertyLinksResponse

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import models.properties.RatepayerPropertyLinksResponse
import org.apache.pekko.http.scaladsl.model.headers.EntityTagRange.*
import org.checkerframework.checker.units.qual.A
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

trait MockHttpV2  extends TestSupport with BeforeAndAfterEach {

  lazy val mockHttpClientV2: HttpClientV2 = Mockito.mock(classOf[HttpClientV2])
  lazy val mockRequestBuilder: RequestBuilder = Mockito.mock(classOf[RequestBuilder])

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttpClientV2)
    reset(mockRequestBuilder)
  }

  def setupMockHttpV2Get[T](url: String)(response: T): OngoingStubbing[Future[T]] = {
    when(mockHttpClientV2
      .get(ArgumentMatchers.eq(url"$url"))(ArgumentMatchers.any()))
      .thenReturn(mockRequestBuilder)

    when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)

    when(mockRequestBuilder.setHeader(any[(String, String)]))
      .thenReturn(mockRequestBuilder)

    when(mockRequestBuilder
      .setHeader(any[(String, String)]))
      .thenReturn(mockRequestBuilder)

    when(mockRequestBuilder
      .execute[T](ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(response))
  }

  def setupMockHttpV2Post[T](url: String)(response: T): OngoingStubbing[Future[T]] = {
    when(mockHttpClientV2.post(ArgumentMatchers.eq(url"$url"))(any()))
      .thenReturn(mockRequestBuilder)
    
    when(mockRequestBuilder.setHeader(any[(String, String)]))
      .thenReturn(mockRequestBuilder)

    when(mockRequestBuilder.withBody(any())(any(), any(), any()))
      .thenReturn(mockRequestBuilder)

    when(mockRequestBuilder.execute[T](any(), any()))
      .thenReturn(Future.successful(response))
  }
  def setupMockHttpV2PostWithHeaderCarrier[T](url: String, headers: Seq[(String, String)])(response: T): OngoingStubbing[Future[T]] = {
    when(mockHttpClientV2.post(ArgumentMatchers.eq(url"$url"))(any()))
      .thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.setHeader(headers: _*))
      .thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.withBody(any())(any(), any(), any()))
      .thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.transform(any()))
      .thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.execute[T](any(), any()))
      .thenReturn(Future.successful(response))
  }


  def setupMockHttpV2FailedPost(url: String): OngoingStubbing[Future[Nothing]] = {
    when(mockHttpClientV2.post(ArgumentMatchers.eq(url"$url"))(any())).thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.execute(any(), any())).thenReturn(Future.failed(new RuntimeException("Request Failed")))
  }

  def setupMockHttpV2FailedPostWithHeaderCarrier(url: String, headers: Seq[(String, String)]): OngoingStubbing[Future[Nothing]] = {
    when(mockHttpClientV2.post(ArgumentMatchers.eq(url"$url"))(any())).thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.setHeader(headers: _*))
      .thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.execute(any(), any())).thenReturn(Future.failed(new RuntimeException("Request Failed")))
  }


  def setupMockFailedHttpV2Get(url: String)(implicit hc: HeaderCarrier): Unit = {
    val builder = mock[RequestBuilder]

    // mock the GET: url must match EXACTLY
    when(mockHttpClientV2.get(ArgumentMatchers.eq(new URL(url)))(any[HeaderCarrier]))
      .thenReturn(builder)

    // mock the execute to fail
    when(builder.execute[Option[RatepayerPropertyLinksResponse]](
      any[HttpReads[Option[RatepayerPropertyLinksResponse]]],
      any[ExecutionContext]
    )).thenReturn(Future.failed(new RuntimeException("boom")))
  }


}
