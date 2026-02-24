/*
 * Copyright 2025 HM Revenue & Customs
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

package helpers

import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, TestSuite}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSCookie, WSRequest}
import play.api.mvc.{Cookie, Session, SessionCookieBaker as CSessionCookieBaker}
import play.api.test.Injecting
import play.api.{Application, Environment, Mode}
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys, client}
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCrypto

trait IntegrationSpecBase extends TestSuite with GuiceOneServerPerSuite with ScalaFutures with IntegrationPatience with Matchers
with WiremockHelper with BeforeAndAfterEach with BeforeAndAfterAll with Eventually with IntegrationTestData with Injecting {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  val mockHost: String = WiremockHelper.wiremockHost
  val mockPort: String = WiremockHelper.wiremockPort.toString
  val mockUrl: String = s"http://$mockHost:$mockPort"

  def config: Map[String, Any] = Map(
    "play.filters.disabled" -> Seq("uk.gov.hmrc.play.bootstrap.frontend.filters.SessionIdFilter"),
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "centralised-authorisation-resource-client.filter.enabled" -> "false",
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "microservice.services.citizen-details.host" -> mockHost,
    "microservice.services.citizen-details.port" -> mockPort,
    "microservice.services.address-lookup.host" -> mockHost,
    "microservice.services.address-lookup.port" -> mockPort,
    "microservice.services.centralised-authorisation-server.host" -> mockHost,
    "microservice.services.centralised-authorisation-server.port" -> mockPort,
    "microservice.services.ngr-notify.host" -> mockHost,
    "microservice.services.ngr-notify.port" -> mockPort,
    "microservice.services.next-generation-rates.host" -> mockHost,
    "microservice.services.next-generation-rates.port" -> mockPort,
    "features.bridgeEnabled" -> true,
    "allowedUsers.emailIds.0" -> "test@test.co.uk"
  )

  lazy val client: WSClient = app.injector.instanceOf[WSClient]

  def buildRequest(path: String): WSRequest =
    client.url(s"http://localhost:$port/ngr-login-register-frontend$path")
      .withHttpHeaders(bakeCookie())
      .withCookies(mockSessionCookie)
      .withFollowRedirects(false)

  private def bakeCookie(sessionData: (String, String)*): (String, String) =
    HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(sessionData.toMap)


  def mockSessionCookie: WSCookie = {

    def makeSessionCookie(session: Session): Cookie = {
      val cookieCrypto   = inject[SessionCookieCrypto]
      val cookieBaker    = inject[CSessionCookieBaker]
      val sessionCookie  = cookieBaker.encodeAsCookie(session)
      val encryptedValue = cookieCrypto.crypto.encrypt(PlainText(sessionCookie.value))
      sessionCookie.copy(value = encryptedValue.value)
    }

    val mockSession = Session(
      Map(
        SessionKeys.lastRequestTimestamp -> System.currentTimeMillis().toString,
        SessionKeys.authToken            -> "mock-bearer-token",
        SessionKeys.sessionId            -> "mock-sessionid"
      )
    )

    val cookie = makeSessionCookie(mockSession)

    new WSCookie() {
      override def name: String = cookie.name

      override def value: String = cookie.value

      override def domain: Option[String] = cookie.domain

      override def path: Option[String] = Some(cookie.path)

      override def maxAge: Option[Long] = cookie.maxAge.map(_.toLong)

      override def secure: Boolean = cookie.secure

      override def httpOnly: Boolean = cookie.httpOnly
    }
  }

  override implicit lazy val app: Application =
    new GuiceApplicationBuilder()
      .in(Environment.simple(mode = Mode.Dev))
      .configure(config)
      .build()

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

}
