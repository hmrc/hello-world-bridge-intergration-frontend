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

import Connector.BridgeIntegrationConnector
import com.google.inject.Inject
import controllers.actions.*
import models.registration.{Name, PhoneNumber, RegisterRatepayer}
import pages.{ContactNumberPage, UserNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import repositories.SessionRepository
import service.CheckAnswers.createSummaryRows
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UniqueIdGenerator
import viewmodels.govuk.summarylist.*
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            sessionRepository: SessionRepository,
                                            bridgeIntegrationConnector: BridgeIntegrationConnector,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          )(implicit ec: ExecutionContext)                         
  extends FrontendBaseController with I18nSupport {

  // GET /check-answers
  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      Ok(view(createSummaryRows(request.userAnswers)))
    }

  /**
   * Submits the ratepayer data to the bridge and redirects based on success.
   */
  private def submitData(
                          userId: String,
                          ratepayerDataOpt: Option[RegisterRatepayer]
                        )(implicit request: Request[AnyContent]): Future[Result] = {
    ratepayerDataOpt match {
      case Some(ratepayerData) =>
        val updatedRatepayerData =
          ratepayerData.copy(
            ratepayerCredId = Some(userId),
            recoveryId      = Some(UniqueIdGenerator.generateId)
          )

        bridgeIntegrationConnector.registerRatePayer(updatedRatepayerData).flatMap { notifySuccess =>
          if (notifySuccess) {
            Future.successful(
              Redirect(routes.IndexController.onPageLoad())
            )
          } else {
            Future.failed(new Exception(s"Failed to send to the bridge for credId: $userId"))
          }
        }

      case None =>
        Future.failed(new Exception("No ratepayer data found in request"))
    }
  }

  // POST /check-answers
  def onSubmit: Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      sessionRepository.get(request.userId).flatMap {
        case None =>
          Future.successful(Redirect(routes.IndexController.onPageLoad()))

        case Some(existingAnswers) =>
          val nameOpt: Option[Name] =
            existingAnswers.get(UserNamePage).map(Name(_))

          val contactNumberOpt: Option[PhoneNumber] =
            existingAnswers.get(ContactNumberPage).map(number => PhoneNumber(number.toString))

          val ratepayerRequest = RegisterRatepayer(
            ratepayerCredId     = None,
            userType            = None,
            agentStatus         = None,
            name                = nameOpt,
            tradingName         = None,
            email               = None,
            nino                = None,
            contactNumber       = contactNumberOpt,
            secondaryNumber     = None,
            address             = None,
            trnReferenceNumber  = None,
            isRegistered        = Some(true),
            recoveryId          = None
          )

          submitData(request.userId, Some(ratepayerRequest)).recover {
            case _ =>
              // Fallback UX on submission failure; adjust as desired
              Redirect(routes.IndexController.onPageLoad())
          }
      }
    }
}
