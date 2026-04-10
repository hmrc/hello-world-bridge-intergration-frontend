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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ExploreRatepayerView
import play.api.Logging
import models.viewModels.property.PropertySummaryList

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExploreRatePayerController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      identify: IdentifierAction,
                                      connector: BridgeIntegrationConnector,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: ExploreRatepayerView,
                                    )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport with Logging  with PropertySummaryList {

  def exploreRatePayer(): Action[AnyContent] =
    identify.async { implicit request =>
      connector.exploreRatePayer().flatMap {
        case Some(value) => Future.successful(Ok(view(createPersonSummaryList(value.persons))))
        case None => throw new Exception("Failed to retrieve person")
      }
    }

}
