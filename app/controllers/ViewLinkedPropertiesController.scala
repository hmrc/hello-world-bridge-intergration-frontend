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
import models.viewModels.property.PropertySummaryList
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ViewLinkedPropertiesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewLinkedPropertiesController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                identify: IdentifierAction,
                                                val controllerComponents: MessagesControllerComponents,
                                                bridgeIntegrationConnector: BridgeIntegrationConnector,
                                                view: ViewLinkedPropertiesView
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging with PropertySummaryList {

  def onPageLoad(): Action[AnyContent] = identify.async {
    implicit request =>
      bridgeIntegrationConnector.getRatepayerProperties().flatMap{
        case Some(ratepayerPropertyLinksResponse) =>
          Future.successful(Ok(view(
            linkedProperties = true,
            createPropertySummaryList(ratepayerPropertyLinksResponse.properties),
            createPersonSummaryList(ratepayerPropertyLinksResponse.persons),
            createRelationshipSummaryList(ratepayerPropertyLinksResponse.relationships)
          )))
        case None =>
          Future.successful(Ok(view(
            linkedProperties = false,
            createPropertySummaryList(List.empty),
            createPersonSummaryList(List.empty),
            createRelationshipSummaryList(List.empty)
          )))
      }.recover{
        case e =>
          logger.error(s"[bridgeIntegrationConnector][getDashboard] Failed for ${request.userId}: ${e.getMessage}")
          Ok(view(
            linkedProperties = false,
            createPropertySummaryList(List.empty),
            createPersonSummaryList(List.empty),
            createRelationshipSummaryList(List.empty)
          ))
      }
  }
}