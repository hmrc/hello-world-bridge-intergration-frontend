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
import models.UserAnswers
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
    with I18nSupport
    with Logging {

  // ==================================================================
  // SERVER‑SIDE AUTHORITATIVE RELATIONSHIP MODEL
  // ==================================================================

  private val relationshipRequest: Relationship =
    Relationship(
      id = Some(13),
      idx = "1.13.1",
      name = "Property Link",
      label = "Ratepayer-ListEntry-Property",
      description =
        "A relationships between LGFA88shd9para4J person-personas and LGFA88 hereditaments for which such personas are obliged to provide LGFA88shd9para4I(1) notofiable information.",
      origination = None,
      termination = None,
      category = CodeMeaning(
        code = Some("LTX-DOM-REL"),
        meaning = Some("Local taxation domain relationship")
      ),
      `type` = CodeMeaning(
        code = Some("LIB"),
        meaning = Some(
          "Liability | One entity is liable for other entity(s)"
        )
      ),
      `class` = CodeMeaning(
        code = Some("LOC"),
        meaning = Some("Local Non Domestic Rating Occupied Hereditament Charge")
      ),
      data = RelationshipData(
        foreign_ids = List.empty,
        foreign_names = List.empty,
        foreign_labels = List.empty,
        manifestations = List(
          RelationshipManifestation(
            artifact_reference = None,
            artifact_code = Some("NRB"),
            artifact_description = None,
            issued_date = None,
            withdrawn_date = None,
            effective_from_date = None,
            effective_to_date = None,
            observed_date = None,
            operative_area_code = None,
            operative_area_name = None,
            protodata_ptr = Some("https://hmrc/sdes/yry64849ree"),
            notes = None
          )
        )
      ),
      protodata = List.empty,
      metadata = Metadata(
        sending = SendingMetadata(
          extracting = MetadataStage(selecting = Map.empty),
          transforming = MetadataStage(
            filtering = Map.empty,
            supplementing = Map.empty,
            recontextualising = Map.empty
          ),
          loading = MetadataStage()
        ),
        receiving = ReceivingMetadata(
          unloading = MetadataStage(),
          transforming = MetadataStage(),
          storing = MetadataStage()
        )
      ),
      compartments = Map.empty,
      items = List(
        RelationshipItem(
          transportation =
            RelationshipItemTransportation(
              path = "/job/compartments/properties/@id=13/data/assessments/@id=13"
            ),
          persistence =
            RelationshipItemPersistence(
              place = "LTX-DOM-AST",
              identifier = Some("13")
            )
        ),
        RelationshipItem(
          transportation =
            RelationshipItemTransportation(
              path = "/job/compartments/persons/@id=16/items/@id=13"
            ),
          persistence =
            RelationshipItemPersistence(
              place = "LTX-DOM-PSA",
              identifier = Some("13")
            )
        )
      )
    )

  // ==================================================================
  // PAGE LOAD
  // ==================================================================

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

    // ✅ MERGE INTO RELATIONSHIP JSON ONLY
    val outboundJson =
      propertyLinksUserAnswersService.mergeIntoOriginalJson(
        originalJson = originalJson,
        answers = answers
      )

    // ✅ CRITICAL SAFETY LOG
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

  // ==================================================================
  // POST
  // ==================================================================

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