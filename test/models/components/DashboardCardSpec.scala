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

package models.components

import helpers.ViewBaseSpec
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.Aliases.{CardTitle, Tag, Text}
import views.html.components.{CardComponent, LinkComponent}

class DashboardCardSpec extends ViewBaseSpec {

  val dashboardCard: DashboardCard = DashboardCard(
    titleKey = "home.yourAccountCard.title",
    captionKey =  Some("home.yourAccountCard.caption"),
    captionKey2 =  Some("home.yourAccountCard.caption2"),
    captionKey3 =  Some("home.yourAccountCard.caption3"),
    voaReference = Some("VOA176292C"),
    tag = Some("home.yourAccountCard.tag"),
    links = Some(
      Seq(
        Link(
          href       = Call(method = "GET",url = "some-href"),
          linkId     = "LinkId-Card",
          messageKey = "home.yourAccountCard.link1",
        )
      )
    )
  )

  "DashboardCard.card" should {
    "build a full Card with all optional values" in {

      val result: Card = DashboardCard.card(dashboardCard)

      result.titleKey mustBe Some(CardTitle(Text("Account home")))
      result.captionKey mustBe Some(CardCaption(Text("Add and manage rating agents.")))
      result.captionKey2 mustBe Some(CardCaption(Text("Rating agents can deal with the VOA and your business rates on your behalf.")))
      result.captionKey3 mustBe Some(CardCaption(Text("Your client code is:")))
      result.voaReference mustBe Some(CardCaption(Text("VOA176292C")))
      result.tag mustBe Some(Tag(content = Text("Action needed")))
      result.links.map(_.links.head.linkId) mustBe Some("LinkId-Card")
      result.links.map(_.links.head.href) mustBe Some(Call(method = "GET",url = "some-href"))
      result.links.map(_.links.head.messageKey) mustBe Some("home.yourAccountCard.link1")
    }

    "build a Card with only mandatory values" in {

      val input = DashboardCard(titleKey = "home.yourAccountCard.title")
      val result: Card = DashboardCard.card(input)

      result.titleKey mustBe Some(CardTitle(Text("Account home")))
      result.captionKey mustBe None
      result.captionKey2 mustBe None
      result.captionKey3 mustBe None
      result.voaReference mustBe None
      result.tag mustBe None
      result.links mustBe None
    }

    "card must render correctly" in {
      val result: Card = DashboardCard.card(dashboardCard)
      CardComponent.f(result)(messages).toString() must not be empty
      CardComponent.render(result, messages).toString() must not be empty
      CardComponent.apply(result).toString() must not be empty
    }

    "link component must render correctly" in {
      val link = Link(
        href       = Call(method = "GET",url = "some-href"),
        linkId     = "LinkId-Card",
        messageKey = "home.yourAccountCard.link1",
      )
      LinkComponent.f(link)(messages).toString() must not be empty
      LinkComponent.apply(link).toString() must not be empty
      LinkComponent.render(link, messages).toString() must not be empty
    }


  }

}
