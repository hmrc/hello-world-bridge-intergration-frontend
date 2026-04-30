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

import base.SpecBase
import connectors.BridgeIntegrationConnector
import models.UserAnswers
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.relationship.PropertyLinkOriginalJsonPage
import play.api.inject.bind
import play.api.libs.json.{JsValue, Json}
import play.api.test.CSRFTokenHelper.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import routes.*
import service.PropertyLinksUserAnswersService
import views.html.CheckYourAnswersRatepayerPropertyLinksView
import uk.gov.hmrc.http.HeaderCarrier

import ch.qos.logback.classic.{Level, Logger}
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.jdk.CollectionConverters.*

class CheckYourAnswersRatepayerPropertyLinksControllerSpec
  extends SpecBase
    with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val mockSessionRepository      = mock[SessionRepository]
  private val mockConnector              = mock[BridgeIntegrationConnector]
  private val mockUserAnswersService     = mock[PropertyLinksUserAnswersService]

  private val validOriginalJson: JsValue =
    Json.obj("items" -> Json.arr(Json.obj("test" -> "value")))

  private val mergedJson: JsValue =
    Json.obj("merged" -> "relationship")

  private def applicationWithAnswers(answers: Option[UserAnswers]) =
    applicationBuilder(answers)
      .overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[BridgeIntegrationConnector].toInstance(mockConnector),
        bind[PropertyLinksUserAnswersService].toInstance(mockUserAnswersService)
      )
      .build()

  def beforeEach(): Unit = {
    reset(mockSessionRepository, mockConnector, mockUserAnswersService)

    when(mockSessionRepository.get(any()))
      .thenReturn(Future.successful(Some(emptyUserAnswers)))
  }

  "CheckYourAnswersRatepayerPropertyLinksController" - {

    // =========================================================
    // onPageLoad
    // =========================================================

    "onPageLoad" - {

      "must render the CheckYourAnswersRatepayerPropertyLinksView" in {

        when(mockUserAnswersService.populateFromRelationship(any(), any()))
          .thenAnswer(_.getArgument[UserAnswers](0))

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val app = applicationWithAnswers(Some(emptyUserAnswers))

        val request =
          FakeRequest(
            GET,
            routes.CheckYourAnswersRatepayerPropertyLinksController.onPageLoad().url
          ).withCSRFToken

        val result = route(app, request).value
        val view =
          app.injector.instanceOf[CheckYourAnswersRatepayerPropertyLinksView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          service.CheckAnswers
            .createRatePayersPropertyLinksSummaryRows(emptyUserAnswers)(
              messages(app)
            )
        )(request, messages(app)).toString

        app.stop()
      }
    }

    // =========================================================
    // postRatepayerPropertyLinks
    // =========================================================

    "postRatepayerPropertyLinks" - {

      "must redirect to Dashboard when submission succeeds" in {

        val answers =
          emptyUserAnswers
            .set(PropertyLinkOriginalJsonPage, validOriginalJson)
            .success.value

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(answers)))

        when(mockUserAnswersService.mergeIntoOriginalJson(any(), any()))
          .thenReturn(mergedJson)

        when(mockConnector.changePropertyLink(any())(any()))
          .thenReturn(Future.successful(true))

        val app = applicationWithAnswers(Some(answers))

        val request =
          FakeRequest(
            POST,
            routes.CheckYourAnswersRatepayerPropertyLinksController
              .postRatepayerPropertyLinks.url
          ).withCSRFToken

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.DashboardController.onPageLoad().url

        verify(mockConnector, times(1))
          .changePropertyLink(eqTo(mergedJson))(any())

        app.stop()
      }

      "must redirect to IndexController when bridge returns false" in {

        val answers =
          emptyUserAnswers
            .set(PropertyLinkOriginalJsonPage, validOriginalJson)
            .success.value

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(answers)))

        when(mockUserAnswersService.mergeIntoOriginalJson(any(), any()))
          .thenReturn(mergedJson)

        when(mockConnector.changePropertyLink(any())(any()))
          .thenReturn(Future.successful(false))

        val app = applicationWithAnswers(Some(answers))

        val request =
          FakeRequest(
            POST,
            routes.CheckYourAnswersRatepayerPropertyLinksController
              .postRatepayerPropertyLinks.url
          ).withCSRFToken

        val result = route(app, request).value
        await(result)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.IndexController.onPageLoad().url

        app.stop()
      }

      "must redirect to IndexController and log error when submission throws" in {

        val answers =
          emptyUserAnswers
            .set(PropertyLinkOriginalJsonPage, validOriginalJson)
            .success.value

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(answers)))

        when(mockUserAnswersService.mergeIntoOriginalJson(any(), any()))
          .thenReturn(mergedJson)

        when(mockConnector.changePropertyLink(any())(any()))
          .thenReturn(Future.failed(new RuntimeException("boom")))

        val rootLogger =
          LoggerFactory
            .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
            .asInstanceOf[Logger]

        rootLogger.setLevel(Level.ERROR)

        val appender = new ListAppender[ILoggingEvent]
        appender.start()
        rootLogger.addAppender(appender)

        val app = applicationWithAnswers(Some(answers))

        val request =
          FakeRequest(
            POST,
            routes.CheckYourAnswersRatepayerPropertyLinksController
              .postRatepayerPropertyLinks.url
          ).withCSRFToken

        val result = route(app, request).value
        await(result)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.IndexController.onPageLoad().url

        appender.list.asScala.exists(_.getLevel == Level.ERROR) mustBe false

        rootLogger.detachAppender(appender)
        appender.stop()
        app.stop()
      }
    }
  }
}
