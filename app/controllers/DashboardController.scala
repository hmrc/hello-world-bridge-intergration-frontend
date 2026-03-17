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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.Status.Approved
import models.dashboard.RatepayerStatusResponse
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DashboardHelper
import views.html.DashboardView

import javax.inject.Inject
import scala.concurrent
import scala.concurrent.{ExecutionContext, Future}

class DashboardController  @Inject()(override val messagesApi: MessagesApi,
                                     identify: IdentifierAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     bridgeIntegrationConnector: BridgeIntegrationConnector,
                                     val controllerComponents: MessagesControllerComponents,
                                     view: DashboardView)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging{

  def onPageLoad(): Action[AnyContent] = identify.async { implicit request =>
    
    bridgeIntegrationConnector.getDashboard().flatMap {
      case Some(answer) if answer.activeRatepayerPersonExists =>
        Future.successful(
          Ok(
            view(
              cards = DashboardHelper.getDashboardCards(answer.activePropertyLinkCount > 0, Approved),
              name = "Registered User"
            )
          )
        )

      case Some(answer) =>
        Future.successful(
          Redirect(routes.IndexController.onPageLoad())
        )

      case None =>
        logger.warn(s"[bridgeIntegrationConnector][getDashboard] user not registered")
        Future.successful(
          Redirect(routes.IndexController.onPageLoad())
        )

    }.recover {
      case e =>
        Redirect(routes.IndexController.onPageLoad())
    }
  }
}