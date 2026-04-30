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
import models.{RelationshipRequestHelper, UserAnswers}
import models.bridge.common.*
import models.bridge.relationhship.*
import models.requests.DataRequest
import pages.relationship.PropertyLinkOriginalJsonPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.*
import repositories.SessionRepository
import service.CheckAnswers.createRatePayersPropertyLinksSummaryRows
import service.PropertyLinksUserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CheckYourAnswersRatepayerPropertyLinksView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersRatepayerPropertyLinksController @Inject()(
                                                                  override val messagesApi: MessagesApi,
                                                                  identify: IdentifierAction,
                                                                  getData: DataRetrievalAction,
                                                                  requireData: DataRequiredAction,
                                                                  sessionRepository: SessionRepository,
                                                                  propertyLinksUserAnswersService: PropertyLinksUserAnswersService,
                                                                  connector: BridgeIntegrationConnector,
                                                                  val controllerComponents: MessagesControllerComponents,
                                                                  view: CheckYourAnswersRatepayerPropertyLinksView
                                                                )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with RelationshipRequestHelper
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData).async { implicit request =>
      val baseAnswers =
        request.userAnswers.getOrElse(UserAnswers(request.userId))
      
      val answersWithOriginalJson =
        baseAnswers
          .get(PropertyLinkOriginalJsonPage)
          .filter(js => (js \ "items").isDefined)
          .getOrElse(Json.toJson(relationshipRequest))

      val hydratedAnswers =
        propertyLinksUserAnswersService
          .populateFromRelationship(
            baseAnswers
              .set(PropertyLinkOriginalJsonPage, answersWithOriginalJson)
              .getOrElse(baseAnswers),
            relationshipRequest
          )

      sessionRepository.set(hydratedAnswers)

      val summary =
        createRatePayersPropertyLinksSummaryRows(hydratedAnswers)

      Future.successful(Ok(view(summary)))
    }
  
  private def submitData(
                          userId: String,
                          originalJson: JsValue,
                          answers: UserAnswers
                        )(implicit request: Request[AnyContent]): Future[Result] = {

    val outboundJson =
      propertyLinksUserAnswersService.mergeIntoOriginalJson(
        originalJson = originalJson,
        answers = answers
      )
    
    logger.info(
      s"""Outbound Relationship JSON:
         |${Json.prettyPrint(outboundJson)}
         |""".stripMargin
    )

    connector.changePropertyLink(outboundJson).flatMap {
      case true =>
        Future.successful(
          Redirect(routes.DashboardController.onPageLoad())
        )
      case false =>
        Future.failed(
          new Exception(s"Bridge rejected relationship for $userId")
        )
    }
  }
  
  def postRatepayerPropertyLinks: Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request: DataRequest[AnyContent] =>

        val originalJson =
          request.userAnswers
            .get(PropertyLinkOriginalJsonPage)
            .filter(js => (js \ "items").isDefined) // ✅ GUARANTEE SHAPE
            .getOrElse(Json.toJson(relationshipRequest))

        submitData(
          userId = request.userId,
          originalJson = originalJson,
          answers = request.userAnswers
        ).recover {
          case ex =>
            logger.error(
              s"Failed to submit property link for user ${request.userId}",
              ex
            )
            Redirect(routes.IndexController.onPageLoad())
        }
    }
}