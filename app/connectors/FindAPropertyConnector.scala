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

import play.api.http.Status
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import config.AppConfig
import forms.FindAPropertyForm
import models.properties.VMVProperties
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.http.ErrorResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FindAPropertyConnector @Inject()(
                                        http: HttpClientV2,
                                        appConfig: AppConfig
                                      )(implicit ec: ExecutionContext)
  extends Logging {

  def findAPropertyPostcodeSearch(
                                   searchParams: FindAPropertyForm
                                 )(implicit hc: HeaderCarrier): Future[Either[ErrorResponse, VMVProperties]] = {

    val urlEndpoint =
      if (appConfig.useStubForVmv) {
        url"${appConfig.bridgeIntegrationStubHost}/bridge-integration-stub/external-ndr-list-api/properties?postcode=${searchParams.postcode.value.toUpperCase.take(4).trim.replaceAll("\\s", "")}"
      } else {
        if (searchParams.propertyName.nonEmpty) {
          val cleanedName = searchParams.propertyName.map(_.replaceAll("['()]", "")).getOrElse("")
          url"${appConfig.vmvAddressLookup}/vmv/rating-listing/api/properties?postcode=${searchParams.postcode.value}&propertyNameNumber=$cleanedName&size=15&searchDirection=FORWARD"
        } else {
          url"${appConfig.vmvAddressLookup}/vmv/rating-listing/api/properties?postcode=${searchParams.postcode.value}&size=15&searchDirection=FORWARD"
        }
      }

    http.get(urlEndpoint)
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK | NOT_FOUND =>
            response.json.validate[VMVProperties] match {
              case JsSuccess(valid, _) => Right(valid)
              case JsError(errors) => Left(ErrorResponse(BAD_REQUEST, s"Json Validation Error: $errors"))
            }
          case _ =>
            Left(ErrorResponse(response.status, response.body))
        }
      }
      .recover {
        case _ =>
          Left(ErrorResponse(Status.INTERNAL_SERVER_ERROR, "Call to VMV find a property failed"))
      }
  }
}

