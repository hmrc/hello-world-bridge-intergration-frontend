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
import models.bridge.property.*
import models.bridge.relationhship.Relationship
import models.dashboard.RatepayerStatusResponse
import models.properties.RatepayerPropertyLinksResponse
import models.registration.RegisterRatepayer
import play.api.http.Status.*
import play.api.i18n.Lang.logger
import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

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

  def changePropertyAssessment(
                                payload: JsValue
                              )(implicit hc: HeaderCarrier): Future[Boolean] = {

    http
      .post(uri(s"property-assessment/123456789567/assessment/27399677000").toURL)
      .withBody(payload)
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK =>
            true

          case NOT_FOUND =>
            logger.warn("Property assessment not found")
            false

          case BAD_REQUEST =>
            logger.warn(s"Invalid property assessment payload: ${response.body}")
            false

          case BAD_GATEWAY =>
            logger.error("Upstream bridge unavailable")
            false

          case INTERNAL_SERVER_ERROR =>
            logger.error(s"Server error from bridge: ${response.body}")
            false

          case other =>
            logger.error(s"Unexpected response status from bridge: $other")
            false
        }
      }
      .recover {
        case ex =>
          logger.error(
            s"Call to property assessment update failed: ${ex.getMessage}",
            ex
          )
          false
      }
  }

  def changePropertyLink(
                          payload: JsValue
                        )(implicit hc: HeaderCarrier): Future[Boolean] = {

    payload.validate[Relationship].fold(
      errors => {
        logger.warn(
          s"""
             |Invalid Relationship payload.
             |Validation errors: ${Json.prettyPrint(JsError.toJson(errors))}
             |Payload received:
             |${Json.prettyPrint(payload)}
             |""".stripMargin
        )
        Future.successful(false)
      },
      _ => {
        http
          .post(uri("property-linking/123456789567/relationship-change/27399677000").toURL)
          .setHeader("Content-Type" -> "application/json")
          .withBody(payload)
          .execute[HttpResponse]
          .map { response =>
            response.status match {
              case OK =>
                true

              case NOT_FOUND =>
                logger.warn("Relationship not found")
                false

              case BAD_REQUEST =>
                logger.warn(
                  s"Backend rejected relationship payload: ${response.body}"
                )
                false

              case BAD_GATEWAY =>
                logger.error("Upstream bridge unavailable")
                false

              case INTERNAL_SERVER_ERROR =>
                logger.error(
                  s"Server error from bridge: ${response.body}"
                )
                false

              case other =>
                logger.error(
                  s"Unexpected response status from bridge: $other, body: ${response.body}"
                )
                false
            }
          }
          .recover {
            case ex =>
              logger.error(
                s"Call to property linking failed: ${ex.getMessage}",
                ex
              )
              false
          }
      }
    )
  }


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


  def getPropertiesForAssessment(
                                  credId: String,
                                  assessmentId: String
                                )(implicit hc: HeaderCarrier): Future[Option[PropertyAssessmentContext]] = {

    val url = uri(s"property-assessment/$credId/assessment/$assessmentId").toURL

    http.get(url).execute[JsValue].map { json =>
     val assessment =
        (json \ "properties")
          .asOpt[List[JsObject]]
          .flatMap(_.headOption)
          .flatMap(prop =>
            (prop \ "data" \ "assessments").asOpt[List[PropertyAssessment]]
          )
          .flatMap(_.headOption)

      assessment.map { a =>
        PropertyAssessmentContext(
          originalJson = json,
          assessment = a
        )
      }
    }.recover {
      case ex =>
        logger.warn(
          s"Failed to retrieve property assessment for credId=$credId",
          ex
        )
        None
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