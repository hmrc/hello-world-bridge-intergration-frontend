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

import connectors.{BridgeIntegrationConnector, FindAPropertyConnector}
import controllers.actions.IdentifierAction
import forms.FindAPropertyBridgeForm.form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.FindAPropertyBridgeRepo
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.{FindAPropertyBridgeView, FindAPropertyView}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FindAPropertyBridgeController @Inject()(findAPropertyBridgeView: FindAPropertyBridgeView,
                                              identify: IdentifierAction,
                                              connector: BridgeIntegrationConnector,
                                              repo: FindAPropertyBridgeRepo,
                                              mcc: MessagesControllerComponents
                                       )(implicit ec: ExecutionContext)
extends FrontendController(mcc) with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    identify.async { implicit request =>
      Future.successful(Ok(findAPropertyBridgeView(form)))
    }

  def onSubmit: Action[AnyContent] =
    identify.async { implicit request =>
      val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      val userId = hc.sessionId.map(_.value).getOrElse("id")
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(findAPropertyBridgeView(formWithErrors))),

        findAPropertyBridge => {
          connector.postcodeSearch("CVW", findAPropertyBridge).flatMap {

            case Right(searchResult) if searchResult.results.records.isEmpty =>
              repo.upsert(userId, searchResult).map { _ =>
                Redirect(routes.NoResultsFoundController.onPageLoad)
              }

            case Right(searchResult) =>
              repo.upsert(userId, searchResult).map { _ =>
                Redirect(routes.PropertyResultsBridgeController.onPageLoad())
              }

            case Left(error) =>
              Future.successful(Status(error.statusCode)(Json.toJson(error)))

          }
        }
      )
    }
}
