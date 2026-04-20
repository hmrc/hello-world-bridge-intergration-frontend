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
import controllers.routes
import models.UserAnswers
import models.requests.DataRequest
import pages.property.PropertyAssessmentOriginalJsonPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.*
import repositories.SessionRepository
import service.CheckAnswers.createPropertySummaryRows
import service.PropertyAssessmentUserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CheckYourAnswersRatepayerPropertyAssessmentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersPropertyAssessmentController @Inject()(
                                                              override val messagesApi: MessagesApi,
                                                              identify: IdentifierAction,
                                                              getData: DataRetrievalAction,
                                                              requireData: DataRequiredAction,
                                                              sessionRepository: SessionRepository,
                                                              connector: BridgeIntegrationConnector,
                                                              propertyAssessmentUserAnswersService: PropertyAssessmentUserAnswersService,
                                                              val controllerComponents: MessagesControllerComponents,
                                                              view: CheckYourAnswersRatepayerPropertyAssessmentView
                                                            )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport with Logging{

  // =========================================================
  // GET – Check Your Answers
  // =========================================================

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData).async { implicit request =>
      val baseAnswers: UserAnswers =
        request.userAnswers.getOrElse(UserAnswers(request.userId))
      connector
        .getPropertiesForAssessment(
          credId = "123456789567",
          assessmentId = "27399677000"
        )
        .map {
          case Some(propertyAssessmentContext) =>
            // -------------------------------------------------
            // 1. Store ORIGINAL inbound JSON in UserAnswers
            //    (only once)
            // -------------------------------------------------
            val answersWithOriginalJson =
              baseAnswers
                .get(PropertyAssessmentOriginalJsonPage)
                .fold {
                  baseAnswers
                    .set(PropertyAssessmentOriginalJsonPage, propertyAssessmentContext.originalJson)
                    .getOrElse(baseAnswers)
                }(_ => baseAnswers)
            // -------------------------------------------------
            // 2. Auto‑populate missing answers from assessment
            // -------------------------------------------------
            val hydratedAnswers =
              propertyAssessmentUserAnswersService
                .populateFromAssessment(
                  answersWithOriginalJson,
                  propertyAssessmentContext.assessment
                )
            // -------------------------------------------------
            // 3. Persist UserAnswers in Mongo
            // -------------------------------------------------
            sessionRepository.set(hydratedAnswers)
            // -------------------------------------------------
            // 4. Build summary FROM UserAnswers
            // -------------------------------------------------
            val summary =
              createPropertySummaryRows(hydratedAnswers)
            Ok(view(summary))
          case None =>
            val summary =
              createPropertySummaryRows(baseAnswers)
            Ok(view(summary))
        }
    }
  
  // =========================================================
  // Internal submit helper
  // =========================================================

  private def submitData(
                          userId: String,
                          originalJson: JsValue,
                          answers: UserAnswers
                        )(implicit request: Request[AnyContent]): Future[Result] = {

    val outboundJson =
      propertyAssessmentUserAnswersService.mergeIntoOriginalJson(
        originalJson = originalJson,
        answers = answers
      )

    connector.changePropertyAssessment(outboundJson).map {
      case true =>
        logger.info(s"Successfully submitted property assessment for user: $userId")
        Redirect(routes.DashboardController.onPageLoad())

      case false =>
        logger.error(s"Failed to send updated property assessment for user: $userId")
        Redirect(routes.IndexController.onPageLoad())
    }
  }
  // =========================================================
  // POST – Submit Assessment
  // =========================================================

  def postRatepayerPropertyAssessment: Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request: DataRequest[AnyContent] =>

        val answers: UserAnswers =
          request.userAnswers

        // -------------------------------------------------
        // 1. Retrieve ORIGINAL inbound JSON from Mongo
        // -------------------------------------------------

        val originalJson: JsValue =
          answers
            .get(PropertyAssessmentOriginalJsonPage)
            .getOrElse {
              throw new IllegalStateException(
                "Original property assessment JSON missing from UserAnswers"
              )
            }

        submitData(
          userId = request.userId,
          originalJson = originalJson,
          answers = answers
        ).recover {
          case ex =>
            logger.error(
              s"Failed to submit property assessment for user ${request.userId}",
              ex
            )
            Redirect(routes.IndexController.onPageLoad())
        }
    }
}