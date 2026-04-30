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

package controllers

import connectors.BridgeIntegrationConnector
import controllers.actions.IdentifierAction
import models.assessment.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.AssessmentPropertiesSortingService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.PropertiesForAssessment
import play.api.libs.json.Json

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class PropertiesController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      identify: IdentifierAction,
                                      connector: BridgeIntegrationConnector,
                                      sorting: AssessmentPropertiesSortingService,
                                      view: PropertiesForAssessment,
                                      mcc: MessagesControllerComponents
                                    )(implicit ec: ExecutionContext)
  extends FrontendController(mcc)
    with I18nSupport {

  val pageSize = 100

  def onPageLoad(): Action[AnyContent] =
    identify.async { implicit request =>
      val page = request.getQueryString("page").flatMap(_.toIntOption).getOrElse(1)
      val sortBy = request.getQueryString("sortBy").getOrElse("AddressASC")

      connector
        .getRatepayerProperties()
        .map {
          case Some(propertyAssessmentContext) =>
            val sorted = sorting.sort(propertyAssessmentContext.properties, sortBy)
            val total = sorted.size
            val from = (page - 1) * pageSize
            val until = from + pageSize
            val pageItems = sorted.slice(from, until)
            Ok(
              view(
                AssessmentProperties(pageItems),
                page,
                total,
                pageSize,
                sortBy
              )
            )
          case None =>
            NotFound("No properties found for this assessment")
        }
        .recover {
          case ex =>
            InternalServerError(
              s"Failed to load assessment properties: ${ex.getMessage}"
            )
        }
    }


  def sort(): Action[AnyContent] =
    identify { implicit request =>
      val sortBy =
        request.body.asFormUrlEncoded
          .flatMap(_.get("sortBy").flatMap(_.headOption))
          .getOrElse("AddressASC")

      Redirect(
        routes.PropertiesController
          .onPageLoad()
          .url + s"?page=1&sortBy=$sortBy"
      )
    }
}


