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

import base.SpecBase
import connectors.BridgeIntegrationConnector
import controllers.routes
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.{ContactNumberPage, UserNamePage}
import play.api.inject.bind
import play.api.test.CSRFTokenHelper.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.CheckYourAnswersView
import ch.qos.logback.classic.{Level, Logger}
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import models.registration.RegisterRatepayer
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters.*
import scala.concurrent.Future

class CheckYourAnswersControllerSpec
  extends SpecBase
    with MockitoSugar {

  private val mockSessionRepository = mock[SessionRepository]
  private val mockBridgeConnector   = mock[BridgeIntegrationConnector]
  private val mockRatepayerService   = mock[RegisterRatepayer]

  private def withCapturedLogs[A](loggerName: String)(block: ListAppender[ILoggingEvent] => A): A = {
    val logger = LoggerFactory.getLogger(loggerName).asInstanceOf[Logger]
    val listAppender = new ListAppender[ILoggingEvent]
    listAppender.start()
    logger.addAppender(listAppender)

    try {
      block(listAppender)
    } finally {
      logger.detachAppender(listAppender)
      listAppender.stop()
    }
  }

  private def applicationWithAnswers(answers: Option[UserAnswers]) =
    applicationBuilder(answers)
      .overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[BridgeIntegrationConnector].toInstance(mockBridgeConnector)
      )
      .build()

  def beforeEach(): Unit = {
    reset(mockSessionRepository, mockBridgeConnector)
    when(mockSessionRepository.get(any()))
      .thenReturn(Future.successful(Some(emptyUserAnswers)))
  }

  "CheckYourAnswersController" - {

    "onPageLoad" - {

      "must return OK and render the CheckYourAnswersView" in {
        val answers = emptyUserAnswers
          .set(UserNamePage, "John Doe").success.value
          .set(ContactNumberPage, "07943009607").success.value

        val application = applicationWithAnswers(Some(answers))
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
          .withCSRFToken

        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          service.CheckAnswers.createSummaryRows(answers)(messages(application))
        )(request, messages(application)).toString

        application.stop()
      }
    }

    "onSubmit" - {

      "must call the bridge connector and redirect to Dashboard on success" in {
        // Mock bridge connector success
        when(mockBridgeConnector.registerRatePayer(any())(any()))
          .thenReturn(Future.successful(true))

        val answers = emptyUserAnswers
          .set(UserNamePage, "John Doe").success.value
          .set(ContactNumberPage, "07943039406").success.value

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(answers)))

        val app = applicationWithAnswers(Some(answers))
        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
          .withCSRFToken

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.DashboardController.onPageLoad().url

        verify(mockBridgeConnector, times(1)).registerRatePayer(any())(any())
        app.stop()
      }

      "must redirect to IndexController if no session data found" in {
        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(None))

        val app = applicationWithAnswers(Some(emptyUserAnswers))

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
          .withCSRFToken

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url

        app.stop()
      }

      "must redirect to IndexController when bridge submission fails" in {
        when(mockBridgeConnector.registerRatePayer(any())(any()))
          .thenReturn(Future.failed(new Exception("bridge failure")))

        val answers = emptyUserAnswers

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(answers)))

        val app = applicationWithAnswers(Some(answers))
        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
          .withCSRFToken

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url

        app.stop()
      }

      "must log info when user is successfully registered with bridge" in {
        when(mockBridgeConnector.registerRatePayer(any())(any()))
          .thenReturn(Future.successful(true))

        val answers = emptyUserAnswers
          .set(UserNamePage, "John Doe").success.value
          .set(ContactNumberPage, "07943039406").success.value

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(answers)))

        val app = applicationWithAnswers(Some(answers))

        val rootLogger =
          LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
            .asInstanceOf[Logger]
        rootLogger.setLevel(Level.INFO)

        val appender = new ListAppender[ILoggingEvent]()
        appender.start()
        rootLogger.addAppender(appender)

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
          .withCSRFToken

        val result = route(app, request).value
        await(result)
        status(result) mustEqual SEE_OTHER

        val infoMessages =
          appender.list.asScala
            .filter(_.getLevel == Level.INFO)
            .map(_.getFormattedMessage)

        infoMessages.exists(_.startsWith("Registered user:")) mustBe true
        rootLogger.detachAppender(appender)
        appender.stop()
        app.stop()
      }

      "must log error when bridge returns false" in {
        when(mockBridgeConnector.registerRatePayer(any())(any()))
          .thenReturn(Future.successful(false))

        val answers = emptyUserAnswers
          .set(UserNamePage, "John Doe").success.value
          .set(ContactNumberPage, "07943039406").success.value

        when(mockSessionRepository.get(any()))
          .thenReturn(Future.successful(Some(answers)))

        val app = applicationWithAnswers(Some(answers))
        val rootLogger =
          LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
            .asInstanceOf[Logger]
        rootLogger.setLevel(Level.ERROR)

        val appender = new ListAppender[ILoggingEvent]()
        appender.start()
        rootLogger.addAppender(appender)

        val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
          .withCSRFToken

        val result = route(app, request).value
        await(result)
        status(result) mustEqual SEE_OTHER

        val errorMessages =
          appender.list.asScala
            .filter(_.getLevel == Level.ERROR)
            .map(_.getFormattedMessage)
        errorMessages.exists(_.startsWith("Failed to send to the bridge for credId:")) mustBe true
        rootLogger.detachAppender(appender)
        appender.stop()
        app.stop()
      }
    }
  }
}