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
import repositories.FindAPropertyBridgeRepo
import service.SortingBridgePropertiesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.PropertyResultsBridgeView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PropertyResultsBridgeController @Inject()(
                                                 identify: IdentifierAction,
                                                 repo: FindAPropertyBridgeRepo,
                                                 sorting: SortingBridgePropertiesService,
                                                 view: PropertyResultsBridgeView,
                                                 mcc: MessagesControllerComponents
                                               )(implicit ec: ExecutionContext)

  extends FrontendController(mcc) with I18nSupport {

  private val pageSize = 10

  def onPageLoad(page: Int, sortBy: String): Action[AnyContent] =
    identify.async { implicit request =>
      repo.findByUserId(request.userId).map {
        case Some(stored) =>
          val sorted = sorting.sort(stored.result.results.records, sortBy)

          val total  = sorted.size
          val from      = (page - 1) * pageSize
          val until     = from + pageSize
          val pageItems = sorted.slice(from, until)

          Ok(view(stored.result, pageItems, page, total, pageSize, sortBy))

        case None =>
          Redirect(routes.PropertyResultsBridgeController.onPageLoad())
      }
    }

  def sort: Action[AnyContent] =
    identify { implicit request =>
      val sortBy =
        request.body.asFormUrlEncoded
          .flatMap(_.get("sortBy").flatMap(_.headOption))
          .getOrElse("AddressASC")

      Redirect(routes.PropertyResultsBridgeController.onPageLoad(1, sortBy))
    }

  def selectProperty(index: Int, sortBy: String): Action[AnyContent] =
    identify.async { implicit request =>
      repo.findByUserId(request.userId).map {
        case Some(stored) =>
          val sorted = sorting.sort(stored.result.results.records, sortBy)
          sorted.lift(index) match {
            case Some(selected) =>
              // selected is a RecordWrapper
              // selected.record.data.list_entry contains the selected property data
              Redirect(routes.PropertyResultsBridgeController.onPageLoad(1, sortBy))
            case None =>
              Redirect(routes.PropertyResultsBridgeController.onPageLoad(1, sortBy))
          }

        case None =>

          Redirect(routes.PropertyResultsBridgeController.onPageLoad())

      }

    }

}