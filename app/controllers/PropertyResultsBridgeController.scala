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

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.FindAPropertyBridgeRepo
import services.SortingPostcodeAddressResultsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.PropertyResultsBridgeView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PropertyResultsBridgeController @Inject()(
                                                 repo: FindAPropertyBridgeRepo,
                                                 sorting: SortingPostcodeAddressResultsService,
                                                 view: PropertyResultsBridgeView,
                                                 mcc: MessagesControllerComponents
                                               )(implicit ec: ExecutionContext)

  extends FrontendController(mcc) with I18nSupport {

  private val pageSize = 10

  def onPageLoad(page: Int, sortBy: String): Action[AnyContent] =
    Action.async { implicit request =>
      val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      val userId = hc.sessionId.map(_.value).getOrElse("id")

      repo.findByUserId(userId).map {
        case Some(stored) =>
          val sortedRecords =
            sorting.sort(stored.properties.results.records.toList, sortBy)

          val totalRecords =
            sortedRecords.size

          val totalPages =
            Math.ceil(totalRecords.toDouble / pageSize).toInt.max(1)

          val safePage =
            page.max(1).min(totalPages)

          val from =
            (safePage - 1) * pageSize

          val until =
            from + pageSize

          val pageRecords =
            sortedRecords.slice(from, until)

          val pagedProperties =
            stored.properties.copy(
              results = stored.properties.results.copy(
                current_page = Some(safePage),
                page_size = Some(pageSize),
                total_results = Some(totalRecords),
                total_pages = Some(totalPages),
                has_next = Some(safePage < totalPages),
                has_previous = Some(safePage > 1),
                records = pageRecords
              )
            )

          Ok(view(pagedProperties, sortBy))

        case None =>
          Redirect(routes.FindAPropertyController.onPageLoad())
      }
    }

  def selectProperty(index: Int, sortBy: String): Action[AnyContent] =
    Action.async { implicit request =>
      val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      val userId = hc.sessionId.map(_.value).getOrElse("id")

      repo.findByUserId(userId).map {
        case Some(stored) =>
          val sortedRecords =
            sorting.sort(stored.properties.results.records.toList, sortBy)

          sortedRecords.lift(index) match {
            case Some(selected) =>
              // TODO: Use selected when ready.
              // Example available values:
              // selected.list_entry.relevant_property.id
              // selected.list_entry.addresses.property_full_address
              // selected.list.collection_authority.ons_code
              // selected.list.id
              // selected.list_entry.valuation.value

              Redirect(routes.FindAPropertyController.onPageLoad())

            case None =>
              Redirect(routes.PropertyResultsController.onPageLoad(1, sortBy))
          }

        case None =>
          Redirect(routes.FindAPropertyController.onPageLoad())
      }
    }
}