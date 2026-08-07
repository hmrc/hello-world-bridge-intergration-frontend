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

import config.{AppConfig, FrontendAppConfig}
import play.api.Logging
import play.api.http.Status
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.bridgeintegration.models.bridge.search.ExploreResult
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExplorePropertyConnector @Inject()(
                                          http: HttpClientV2,
                                          appConfig: FrontendAppConfig
                                        )(implicit ec: ExecutionContext)
  extends Logging {

  def explore()(
    implicit hc: HeaderCarrier
  ): Future[Either[ErrorResponse, ExploreResult]] = {

    val urlEndpoint =
      url"${appConfig.bridgeIntegration}/bridge-integration/explore"

    http.get(urlEndpoint)
      .execute[HttpResponse]
      .map { response =>

        response.status match {

          case OK =>
            response.json.validate[ExploreResult] match {

              case JsSuccess(valid, _) =>
                Right(valid)

              case JsError(errors) =>
                Left(
                  ErrorResponse(
                    Status.BAD_REQUEST,
                    s"Json Validation Error: $errors"
                  )
                )
            }

          case _ =>
            Left(
              ErrorResponse(
                response.status,
                response.body
              )
            )
        }
      }
      .recover {
        case ex =>
          logger.error(
            s"Call to bridge-integration /explore failed: ${ex.getMessage}",
            ex
          )

          Left(
            ErrorResponse(
              INTERNAL_SERVER_ERROR,
              "Call to bridge-integration explore failed"
            )
          )
      }
  }
}