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

import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{BAD_GATEWAY, BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.bridgeintegration.models.bridge.search.*
import uk.gov.hmrc.http.HttpReads
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class ExplorePropertyConnectorSpec
  extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures
    with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
  private val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]
  private val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  private val bridgeIntegrationBaseUrl = "http://localhost:11111"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttpClient, mockRequestBuilder, mockAppConfig)
  }

  private val connector =
    new ExplorePropertyConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  private def mockGet(response: Future[HttpResponse]): Unit = {
    when(mockAppConfig.bridgeIntegration)
      .thenReturn(bridgeIntegrationBaseUrl)

    when(mockHttpClient.get(any[URL])(any[HeaderCarrier]))
      .thenReturn(mockRequestBuilder)

    when(
      mockRequestBuilder.execute(any[HttpReads[HttpResponse]],
    any[ExecutionContext])

    ).thenReturn(response)
  }

  private val exploreResult: ExploreResult =
    ExploreResult(
      list = ValuationList(
        id = Id(Some("123456789567")),
        classification = Classification(
          code = Some(""),
          meaning = Some("")
        ),
        country = Some(
          Country(
            ons_code = Some("W92000004"),
            ons_code_label = Some("Wales | Cymru")
          )
        ),
        collection_authority = CollectionAuthority(
          ons_code = Some("W07000064"),
          ons_code_label = Some("Ceredigion | Ceredigion")
        ),
        inforcement_period = Some(
          InforcementPeriod(
            commencement_date = Some("20050401"),
            expiration_date = None
          )
        ),
        compilation_date = None,
        valuation_date = None,
        total_of_all_valuations = None
      ),
      list_entry = ListEntry(
        id = Some(
          Id(
            value = Some("123456789567")
          )
        ),
        designated_person = Some(
          DesignatedPerson(
            name = Some("Designated Person 2"),
            address = Some("1 Test Street, London"),
            company_number = Some("COMP2")
          )
        ),
        relevant_property = Some(
          RelevantProperty(
            vos_property_id = Some("VOS-2")
          )
        ),
        use = Some(
          Use(
            description = Some("General Commercial Use"),
            composite_ind = Some("N"),
            part_exempt_ind = Some("N")
          )
        ),
        valuation = Valuation(
          value = Some("D"),
          method = Some(
            Method(
              code = None,
              meaning = None
            )
          ),
          previous = Some("STD")
        ),
        period = Some(
          Period(
            effective_from_date = Some("20050401"),
            effective_to_date = None
          )
        ),
        administration = Some(
          Administration(
            alteration_date = Some("20230401"),
            alteration_seq_no = Some("1"),
            entry_seq_no = Some("1"),
            judicially_ordered_by = None,
            transitionally_certified = Some("N"),
            collection_authority_ref = None
          )
        ),
        workflow = Some(
          Workflow(
            creating_job_id = Some("R5R875-B52D043-F767863-66ZZZ")
          )
        ),
        addresses = Addresses(
          property_full_address = Some("4 Clos y Fedwen, Cardiff, CF14 0AA")
        ),
        property = Some(
          Property(
            improvement_ind = None
          )
        )
      )
    )

  "ExplorePropertyConnector .explore" should {

    "return Right ExploreResult when bridge-integration returns 200 OK with valid JSON" in {
      val httpResponse =
        HttpResponse(
          status = OK,
          json = Json.toJson(exploreResult),
          headers = Map.empty
        )

      mockGet(Future.successful(httpResponse))

      val result = connector.explore().futureValue

      result shouldBe Right(exploreResult)
    }

    "return BadRequest ErrorResponse when bridge-integration returns 200 OK with invalid JSON" in {
      val invalidJson =
        Json.obj(
          "invalid" -> "json"
        )

      val httpResponse =
        HttpResponse(
          status = OK,
          json = invalidJson,
          headers = Map.empty
        )

      mockGet(Future.successful(httpResponse))

      val result = connector.explore().futureValue

      result.isLeft shouldBe true

      val error = result.left.toOption.get

      error.statusCode shouldBe BAD_REQUEST
      error.message should include("Json Validation Error")
    }

    "return ErrorResponse when bridge-integration returns a non-200 response" in {
      val httpResponse =
        HttpResponse(
          status = BAD_GATEWAY,
          body = "Upstream service unavailable"
        )

      mockGet(Future.successful(httpResponse))

      val result = connector.explore().futureValue

      result shouldBe Left(
        ErrorResponse(
          BAD_GATEWAY,
          "Upstream service unavailable"
        )
      )
    }

    "return InternalServerError ErrorResponse when the call to bridge-integration fails" in {
      mockGet(Future.failed(new RuntimeException("connection failed")))

      val result = connector.explore().futureValue

      result shouldBe Left(
        ErrorResponse(
          INTERNAL_SERVER_ERROR,
          "Call to bridge-integration explore failed"
        )
      )
    }
  }
}