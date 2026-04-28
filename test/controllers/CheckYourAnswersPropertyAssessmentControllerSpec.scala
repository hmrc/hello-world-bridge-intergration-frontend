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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import pages.property.PropertyAssessmentOriginalJsonPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.CSRFTokenHelper.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import routes.*
import service.PropertyAssessmentUserAnswersService
import views.html.CheckYourAnswersRatepayerPropertyAssessmentView

import ch.qos.logback.classic.{Level, Logger}
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.jdk.CollectionConverters.*

class CheckYourAnswersPropertyAssessmentControllerSpec
  extends SpecBase
    with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val mockSessionRepository       = mock[SessionRepository]
  private val mockBridgeConnector         = mock[BridgeIntegrationConnector]
  private val mockUserAnswersService      = mock[PropertyAssessmentUserAnswersService]

  private val originalJson = Json.obj("original" -> "json")
  private val mergedJson   = Json.obj("merged" -> "json")

  private def applicationWithAnswers(answers: Option[UserAnswers]) =
    applicationBuilder(answers)
      .overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[BridgeIntegrationConnector].toInstance(mockBridgeConnector),
        bind[PropertyAssessmentUserAnswersService].toInstance(mockUserAnswersService)
      )
      .build()

  def beforeEach(): Unit = {
    reset(mockSessionRepository, mockBridgeConnector, mockUserAnswersService)

    when(mockSessionRepository.get(any()))
      .thenReturn(Future.successful(Some(emptyUserAnswers)))
  }

  "CheckYourAnswersPropertyAssessmentController" - {

    "onPageLoad" - {

      "must return OK and render the CheckYourAnswersRatepayerPropertyAssessmentView" in {

        when(mockBridgeConnector.getPropertiesForAssessment(any(), any())(any()))
          .thenReturn(Future.successful(None))

        val application = applicationWithAnswers(Some(emptyUserAnswers))

        val request =
          FakeRequest(
            GET,
            routes.CheckYourAnswersPropertyAssessmentController.onPageLoad().url
          ).withCSRFToken

        val result = route(application, request).value
        val view   =
          application.injector.instanceOf[CheckYourAnswersRatepayerPropertyAssessmentView]

        status(result) mustEqual OK
        (contentAsString(result)) mustEqual view(
          service.CheckAnswers.createPropertySummaryRows(emptyUserAnswers)(
            messages(application)
          )
        )(request, messages(application)).toString

        application.stop()
      }
    }

    "postRatepayerPropertyAssessment" - {

      "must submit successfully and redirect to Dashboard when bridge returns true" in {

        val answers =
          emptyUserAnswers
            .set(PropertyAssessmentOriginalJsonPage, originalJson)
            .success.value

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(answers)))

        when(mockUserAnswersService.mergeIntoOriginalJson(any(), any()))
          .thenReturn(mergedJson)

        when(mockBridgeConnector.changePropertyAssessment(any())(any()))
          .thenReturn(Future.successful(true))

        val app = applicationWithAnswers(Some(answers))

        val request =
          FakeRequest(
            POST,
            routes.CheckYourAnswersPropertyAssessmentController.postRatepayerPropertyAssessment.url
          ).withCSRFToken

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.DashboardController.onPageLoad().url

        verify(mockBridgeConnector, times(1))
          .changePropertyAssessment(eqTo(mergedJson))(any())

        app.stop()
      }

      "must redirect to IndexController when bridge returns false" in {

        val answers =
          emptyUserAnswers
            .set(PropertyAssessmentOriginalJsonPage, originalJson)
            .success.value

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(answers)))

        when(mockUserAnswersService.mergeIntoOriginalJson(any(), any()))
          .thenReturn(mergedJson)

        when(mockBridgeConnector.changePropertyAssessment(any())(any()))
          .thenReturn(Future.successful(false))

        val app = applicationWithAnswers(Some(answers))

        val request =
          FakeRequest(
            POST,
            routes.CheckYourAnswersPropertyAssessmentController.postRatepayerPropertyAssessment.url
          ).withCSRFToken

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.IndexController.onPageLoad().url

        app.stop()
      }

      "must redirect to IndexController and log error when submission throws" in {

        val answers =
          emptyUserAnswers
            .set(PropertyAssessmentOriginalJsonPage, originalJson)
            .success.value

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(answers)))

        when(mockUserAnswersService.mergeIntoOriginalJson(any(), any()))
          .thenReturn(mergedJson)

        when(mockBridgeConnector.changePropertyAssessment(any())(any()))
          .thenReturn(Future.failed(new RuntimeException("boom")))

        val rootLogger =
          LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
            .asInstanceOf[Logger]
        rootLogger.setLevel(Level.ERROR)

        val appender = new ListAppender[ILoggingEvent]
        appender.start()
        rootLogger.addAppender(appender)

        val app = applicationWithAnswers(Some(answers))

        val request =
          FakeRequest(
            POST,
            routes.CheckYourAnswersPropertyAssessmentController
              .postRatepayerPropertyAssessment.url
          ).withCSRFToken


        val result = route(app, request).value
        await(result)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.IndexController.onPageLoad().url

        app.stop()
      }

      "must throw IllegalStateException when original JSON is missing" in {

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(emptyUserAnswers)))

        val app = applicationWithAnswers(Some(emptyUserAnswers))

        val request =
          FakeRequest(
            POST,
            routes.CheckYourAnswersPropertyAssessmentController.postRatepayerPropertyAssessment.url
          ).withCSRFToken

        assertThrows[IllegalStateException] {
          await(route(app, request).value)
        }

        app.stop()
      }
    }
  }
}