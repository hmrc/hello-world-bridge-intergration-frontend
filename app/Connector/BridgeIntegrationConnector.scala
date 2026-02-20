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

package Connector

import config.FrontendAppConfig
import models.registration.RegisterRatepayer
import play.api.http.Status.{ACCEPTED, OK}
import play.api.i18n.Lang.logger
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
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
    http.post(uri(s"register-ratepayer/${"123456789123"}").toURL)
      .withBody(Json.toJson(ratepayerRegistration))
      .execute[HttpResponse]
      .map(_.status == ACCEPTED)
      .recover { case ex: Exception =>
        logger.error(s"Call to ngr-notify ratepayer failed: ${ex.getMessage}")
        false
      }
  }
}
