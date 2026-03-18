package controllers

import base.SpecBase
import connectors.BridgeIntegrationConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.*
import play.api.libs.json.Json
import play.api.inject.bind
import play.api.inject
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import views.html.PropertiesForAssessmentView

import scala.concurrent.Future

class PropertiesControllerSpec
  extends SpecBase
    with MockitoSugar {

  private val mockConnector = mock[BridgeIntegrationConnector]

  private def application =
    applicationBuilder(None)
      .overrides(
        bind[BridgeIntegrationConnector].toInstance(mockConnector)
      )
      .build()

  private val credId = "123"
  private val assessmentId = "456"

  "PropertiesController.getPropertiesForAssessment" - {

    "must return OK and render the view when connector returns JSON" in {
      val app = application

      val json = Json.obj("foo" -> "bar")

      when(mockConnector.getPropertiesForAssessment(any(), any())(any()))
        .thenReturn(Future.successful(json))

      val request =
        FakeRequest(GET, routes.PropertiesController.getPropertiesForAssessment(credId, assessmentId).url)
          .withCSRFToken

      val result = route(app, request).value
      val view = app.injector.instanceOf[PropertiesForAssessmentView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(json)(request, messages(app)).toString

      app.stop()
    }

    "must return INTERNAL_SERVER_ERROR when connector throws" in {
      val app = application

      when(mockConnector.getPropertiesForAssessment(any(), any())(any()))
        .thenReturn(Future.failed(new RuntimeException("test crash")))

      val request =
        FakeRequest(GET, routes.PropertiesController.getPropertiesForAssessment(credId, assessmentId).url)
          .withCSRFToken

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      app.stop()
    }
  }
}

