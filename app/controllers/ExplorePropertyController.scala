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

import connectors.ExplorePropertyConnector
import controllers.actions.IdentifierAction
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.ExplorePropertyRepo
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.ExplorePropertyView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExplorePropertyController @Inject()(
                                           view: ExplorePropertyView,
                                           identify: IdentifierAction,
                                           connector: ExplorePropertyConnector,
                                           repo: ExplorePropertyRepo,
                                           mcc: MessagesControllerComponents
                                         )(implicit ec: ExecutionContext)
  extends FrontendController(mcc)
    with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] =
    identify.async { implicit request =>

      connector.explore().flatMap {

        case Right(result) =>

          logger.info(s"Explore result received: $result")

          repo.upsert(request.userId, result).map { _ =>
            Ok(view(result))
          }

        case Left(error) =>

          logger.error(
            s"Explore call failed. Status=${error.statusCode}, message=${error.message}"
          )

          Future.successful(
            Status(error.statusCode)(error.message)
          )
      }
    }
}