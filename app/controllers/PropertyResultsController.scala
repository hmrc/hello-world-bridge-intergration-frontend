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

import controllers.actions.IdentifierAction
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.FindAPropertyRepo
import service.SortingVMVPropertiesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.PropertyResultsView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

class PropertyResultsController @Inject()(
                                           identify: IdentifierAction,
                                           repo: FindAPropertyRepo,
                                           sorting: SortingVMVPropertiesService,
                                           view: PropertyResultsView,
                                           mcc: MessagesControllerComponents
                                         )(implicit ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  private val pageSize = 10

  def onPageLoad(page: Int, sortBy: String): Action[AnyContent] =
    identify.async { implicit request =>
      repo.findByUserId(request.userId).map {
        case Some(stored) =>
          val sorted = sorting.sort(stored.properties.properties.toList, sortBy)
          val total  = sorted.size

          val from   = (page - 1) * pageSize
          val until  = from + pageSize
          val pageItems = sorted.slice(from, until)

          Ok(view(stored.properties, pageItems, page, total, pageSize, sortBy))

        case None =>
          Redirect(routes.FindAPropertyController.onPageLoad())
      }
    }

  def sort: Action[AnyContent] =
    identify { implicit request =>
      val sortBy =
        request.body.asFormUrlEncoded
          .flatMap(_.get("sortBy").flatMap(_.headOption))
          .getOrElse("AddressASC")

      Redirect(routes.PropertyResultsController.onPageLoad( 1, sortBy))
    }
  
  def selectProperty(index: Int, sortBy: String): Action[AnyContent] =
    identify.async { implicit request =>
      repo.findByUserId(request.userId).map {
        case Some(stored) =>
          val sorted = sorting.sort(stored.properties.properties.toList, sortBy)

          sorted.lift(index) match {
            case Some(selected) =>
              // TODO: Replace when ready
              Redirect(routes.FindAPropertyController.onPageLoad())
            case None =>
              Redirect(routes.PropertyResultsController.onPageLoad(1, sortBy))
          }

        case None =>
          Redirect(routes.FindAPropertyController.onPageLoad())
      }
    }
}



