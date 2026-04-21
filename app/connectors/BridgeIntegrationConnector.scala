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

package connectors

import config.FrontendAppConfig
import models.bridge.person.Persons
import models.dashboard.RatepayerStatusResponse
import models.properties.RatepayerPropertyLinksResponse
import models.assessment.AssessmentPropertiesResponse
import models.registration.RegisterRatepayer
import play.api.http.Status.*
import play.api.i18n.Lang.logger
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import play.api.libs.ws.writeableOf_JsValue

import java.net.URI
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class BridgeIntegrationConnector @Inject()(
                                    http: HttpClientV2,
                                    appConfig: FrontendAppConfig
                                  )(implicit ec: ExecutionContext) {

  private def uri(path: String) = new URI(s"${appConfig.bridgeIntegration}/bridge-integration/$path")

  def isAllowedInPrivateBeta(credId: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    http.get(uri(s"allowed-in-private-beta/$credId").toURL)
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK =>
            (response.json \ "allowed").asOpt[Boolean].getOrElse(false)
          case _ =>
            false
        }
      }
  }

  def registerRatePayer(ratepayerRegistration: RegisterRatepayer)
                       (implicit hc: HeaderCarrier): Future[Boolean] = {

    http.post(uri(s"register-ratepayer/123456789567").toURL)
      .withBody(Json.toJson(ratepayerRegistration))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK => true
          case NOT_FOUND =>
            logger.warn("Ratepayer not found")
            false
          case BAD_REQUEST =>
            logger.warn("Invalid register ratepayer request")
            false
          case BAD_GATEWAY =>
            logger.error("Upstream service unavailable")
            false
          case INTERNAL_SERVER_ERROR =>
            logger.error(s"Server error: ${response.body}")
            false
          case other =>
            logger.error(s"Unexpected response status: $other")
            false
        }
      }
      .recover {
        case ex: Exception =>
          logger.error(s"Call to ngr-notify register-ratepayer failed: ${ex.getMessage}", ex)
          false
      }
  }

  //123456789123
  def getDashboard(credId: String = "123456789567")
                  (implicit hc: HeaderCarrier): Future[Option[RatepayerStatusResponse]] = {
    val url = uri(s"dashboard/${credId}").toURL
    http.get(url)
      .execute[Option[RatepayerStatusResponse]]
      .recover {
        case ex =>
          logger.warn(s"Failed to retrieve dashboard for credId=$credId: ${ex.getMessage}")
          None
      }
  }

  def exploreRatePayer(credId: String = "123456789567")
                      (implicit hc: HeaderCarrier): Future[Option[Persons]] = {
    val url = uri(s"explore-ratepayer/$credId").toURL
    http.get(url)
      .execute[Option[Persons]]
      .recover {
        case ex =>
          logger.warn(s"Failed to retrieve explore ratepayer for credId=$credId: ${ex.getMessage}")
          None
      }
  }


  def getProperties(implicit hc: HeaderCarrier): Future[Option[RatepayerPropertyLinksResponse]] = {
    http.get(uri(s"properties").toURL)
      .execute[Option[RatepayerPropertyLinksResponse]]
      .recover {
        case ex =>
          logger.warn(s"Failed to retrieve properties: ${ex.getMessage}")
          None
      }
  }

  def getRatepayerProperties(credId: String = "123456789567")
                            (implicit hc: HeaderCarrier): Future[Option[RatepayerPropertyLinksResponse]] = {
    http.get(uri(s"ratepayer-properties/$credId").toURL)
      .execute[Option[RatepayerPropertyLinksResponse]]
      .recover {
        case ex =>
          logger.warn(s"Failed to retrieve ratepayer properties for credId=$credId: ${ex.getMessage}")
          None
      }
  }

  def getPropertiesForAssessmentJob(
                                     credId: String,
                                     assessmentId: String
                                   )(implicit hc: HeaderCarrier): Future[JsValue] = {

    val url = uri(s"properties/$credId/assessment/$assessmentId").toURL

    http.get(url)
      .execute[JsValue]
      .recover {
        case ex =>
          logger.warn(s"Failed to retrieve properties for credId=$credId assessment=$assessmentId: ${ex.getMessage}")
          Json.obj("error" -> "Unable to fetch properties")
      }
  }

  def getPropertiesForAssessment(
                                  credId: String,
                                  assessmentId: String
                                )(implicit hc: HeaderCarrier): Future[AssessmentPropertiesResponse] = {

    val url = uri(s"ratepayer-properties/$credId/assessment/$assessmentId").toURL

    http.get(url)
      .execute[AssessmentPropertiesResponse]
      .recover {
        case ex =>
          logger.warn(
            s"Failed to retrieve properties for credId=$credId assessment=$assessmentId: ${ex.getMessage}"
          )
          throw ex
      }
  }


  def getRatepayerPropertyLinks(
                                 credId: String,
                                 assessmentId: String
                               )(implicit hc: HeaderCarrier
                               ): Future[JsValue] = {

    val url = uri(s"property-link-job/$credId/assessment/$assessmentId").toURL
    http
      .get(url)
      .execute[JsValue]
      .recover {
        case ex =>
          logger.warn(
            s"Failed to retrieve property links for person=$credId: ${ex.getMessage}"
          )
          Json.obj("error" -> "Unable to fetch property links")
      }
  }
}