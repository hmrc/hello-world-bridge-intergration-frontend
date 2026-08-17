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
import forms.*
import models.bridge.person.Persons
import models.bridge.property.{Property as BridgeProperty, *}
import models.properties.*
import models.bridge.relationhship.Relationship
import models.dashboard.RatepayerStatusResponse
import models.properties.RatepayerPropertyLinksResponse
import models.registration.RegisterRatepayer
import play.api.Logging
import play.api.http.Status.*
import play.api.http.Status
import play.api.libs.json.*
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import java.net.URI
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class BridgeIntegrationConnector @Inject()(
                                            http: HttpClientV2,
                                            appConfig: FrontendAppConfig
                                          )(implicit ec: ExecutionContext) extends Logging {

  private def uri(path: String): URI =
    new URI(s"${appConfig.bridgeIntegration}/bridge-integration/$path")

  def isAllowedInPrivateBeta(
                              credId: String
                            )(implicit hc: HeaderCarrier): Future[Boolean] = {
    http
      .get(uri(s"allowed-in-private-beta/$credId").toURL)
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

  def registerRatePayer(
                         ratepayerRegistration: RegisterRatepayer
                       )(implicit hc: HeaderCarrier): Future[Boolean] = {
    http
      .post(uri("register-ratepayer/123456789567").toURL)
      .withBody(Json.toJson(ratepayerRegistration))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK =>
            true

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
      .post(uri("property-assessment/123456789567/assessment/27399677000").toURL)
      .setHeader("Content-Type" -> "application/json")
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
                logger.warn(s"Backend rejected relationship payload: ${response.body}")
                false

              case BAD_GATEWAY =>
                logger.error("Upstream bridge unavailable")
                false

              case INTERNAL_SERVER_ERROR =>
                logger.error(s"Server error from bridge: ${response.body}")
                false

              case other =>
                logger.error(s"Unexpected response status from bridge: $other, body: ${response.body}")
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

  def getDashboard(
                    credId: String = "123456789567"
                  )(implicit hc: HeaderCarrier): Future[Option[RatepayerStatusResponse]] = {
    http
      .get(uri(s"dashboard/$credId").toURL)
      .execute[Option[RatepayerStatusResponse]]
      .recover {
        case ex =>
          logger.warn(s"Failed to retrieve dashboard for credId=$credId: ${ex.getMessage}")
          None
      }
  }

  def exploreRatePayer(
                        credId: String = "123456789567"
                      )(implicit hc: HeaderCarrier): Future[Option[Persons]] = {
    http
      .get(uri(s"explore-ratepayer/$credId").toURL)
      .execute[Option[Persons]]
      .recover {
        case ex =>
          logger.warn(s"Failed to retrieve explore ratepayer for credId=$credId: ${ex.getMessage}")
          None
      }
  }

  def getProperties(
                     implicit hc: HeaderCarrier
                   ): Future[Option[RatepayerPropertyLinksResponse]] = {
    http
      .get(uri("properties").toURL)
      .execute[Option[RatepayerPropertyLinksResponse]]
      .recover {
        case ex =>
          logger.warn(s"Failed to retrieve properties: ${ex.getMessage}")
          None
      }
  }

  def getRatepayerProperties(
                              credId: String = "123456789567"
                            )(implicit hc: HeaderCarrier): Future[Option[RatepayerPropertyLinksResponse]] = {
    http
      .get(uri(s"ratepayer-properties/$credId").toURL)
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

    http
      .get(url)
      .execute[JsValue]
      .map { json =>
        val assessmentOpt =
          (json \ "properties")
            .asOpt[List[BridgeProperty]]
            .flatMap(_.headOption)

        assessmentOpt.map { assessment =>
          PropertyAssessmentContext(
            originalJson = json,
            assessment = assessment
          )
        }
      }
      .recover {
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
                               )(implicit hc: HeaderCarrier): Future[JsValue] = {
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

  def findPropertyPostcodeSearch(
                                  searchParams: FindAPropertyForm
                                )(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, PostcodeSearchResult]] = {
    val urlEndpoint =
      if (appConfig.useStubForVmv) {
        uri(s"postcode/${searchParams.postcode.value.toUpperCase.replaceAll("\\s", "")}").toURL
      } else {
        if (searchParams.propertyName.nonEmpty) {
          val cleanedName =
            searchParams.propertyName
              .map(_.replaceAll("['()]", ""))
              .getOrElse("")

          url"${appConfig.vmvAddressLookup}/vmv/rating-listing/api/properties?postcode=${searchParams.postcode.value}&propertyNameNumber=$cleanedName&size=15&searchDirection=FORWARD"
        } else {
          url"${appConfig.vmvAddressLookup}/vmv/rating-listing/api/properties?postcode=${searchParams.postcode.value}&size=15&searchDirection=FORWARD"
        }
      }

    http
      .get(urlEndpoint)
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK | NOT_FOUND =>
            response.json.validate[PostcodeSearchResult] match {
              case JsSuccess(valid, _) =>
                Right(valid)

              case JsError(errors) =>
                logger.warn(
                  s"Json validation failed for postcode search response. Errors: $errors. Body: ${response.body}"
                )
                Left(ErrorResponse(BAD_REQUEST, s"Json Validation Error: $errors"))
            }

          case _ =>
            Left(ErrorResponse(response.status, response.body))
        }
      }
      .recover {
        case NonFatal(ex) =>
          logger.error("Call to VMV find a property failed", ex)
          Left(ErrorResponse(Status.INTERNAL_SERVER_ERROR, "Call to VMV find a property failed"))
      }
  }

  def postcodeSearch(
                      searchParams: FindAPropertyBridgeForm
                    )(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, PostcodeSearchResult]] = {
    val postcode: String =
      searchParams.postcode.value.trim.toUpperCase

    val normalisedPostcode =
      postcode.replaceAll("\\s+", "").toUpperCase

    val url =
      uri(s"postcode/$normalisedPostcode/CVW").toURL

    logger.info(
      Console.GREEN +
        s"[BridgeIntegrationConnector][postcodeSearch] Calling backend postcode search url=$url" + Console.RESET
    )

    http
      .get(url)
      .setHeader("Content-Type" -> "application/json")
      .execute[HttpResponse]
      .map { response =>
        logger.info(s"[BridgeIntegrationConnector][postcodeSearch] Response Status=${response.status}, body=${response.body}")
        response.status match {
          case OK =>
            response.json.validate[PostcodeSearchResult] match {
              case JsSuccess(result, _) =>
                Right(result)

              case JsError(errors) =>
                Left(ErrorResponse(BAD_REQUEST, s"Json Validation Error: $errors"))
            }

          case NOT_FOUND =>
            response.json.validate[PostcodeSearchResult] match {
              case JsSuccess(result, _) =>
                Right(result)

              case JsError(_) =>
                Left(ErrorResponse(NOT_FOUND, response.body))
            }

          case BAD_REQUEST =>
            Left(ErrorResponse(BAD_REQUEST, response.body))

          case status if status >= INTERNAL_SERVER_ERROR =>
            Left(ErrorResponse(status, response.body))

          case status =>
            Left(ErrorResponse(status, response.body))
        }
      }

      .recover {

        case e: UpstreamErrorResponse =>
          logger.error(Console.RED +
            s"[BridgeIntegrationConnector][postcodeSearch] Upstream status=${e.statusCode}, message=${e.message}" + Console.RESET,
            e
          )
          Left(ErrorResponse(e.statusCode, e.message))

        case NonFatal(ex) =>
          logger.error(Console.BLUE +
            s"[BridgeIntegrationConnector][postcodeSearch] Unexpected error calling postcode search: ${ex.getMessage}" + Console.RESET,
            ex
          )
          Left(ErrorResponse(INTERNAL_SERVER_ERROR, "Call to Bridge postcode search failed"))
      }
  }
}