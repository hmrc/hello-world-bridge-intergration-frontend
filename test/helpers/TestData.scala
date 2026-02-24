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

import play.api.libs.json.{JsValue, Json}
import models.registration.*
import models.*
import models.registration.RatepayerType.Individual

import java.time.{Instant, LocalDate}

trait TestData {
  
  val testRegistrationModel: RegisterRatepayer =
    RegisterRatepayer(
      userType = Some(Individual),
      agentStatus = Some(AgentStatus.Agent),
      name = Some(Name("John Doe")),
      tradingName = Some(TradingName("CompanyLTD")),
      email = Some(Email("JohnDoe@digital.hmrc.gov.uk")),
      contactNumber = Some(PhoneNumber("07123456789")),
      secondaryNumber = Some(PhoneNumber("07123456789")),
      address = Some(
        Address(line1 = "99",
          line2 = Some("Wibble Rd"),
          town = "Worthing",
          county = Some("West Sussex"),
          postcode = Postcode("BN110AA")
        )
      ),
      trnReferenceNumber = Some(TRNReferenceNumber(ReferenceType.Nino, "12345")),
      isRegistered = Some(false)
    )
  
  val testPostcode: Postcode = Postcode(value = "BH1 7EY")
  val time: Instant = Instant.now()
  val nameModel: Name = Name("Lovely Fella")

  val testAddress: Address =
    Address(
      line1 = "99",
      line2 = Some("Wibble Rd"),
      town = "Worthing",
      county = Some("West Sussex"),
      postcode = Postcode("BN110AA")
    )

  val nameJson: JsValue = Json.parse(
    """
      |{"value":"Lovely Fella"}
      |""".stripMargin)
    
  val contactNumberModel: PhoneNumber = PhoneNumber("0300 200 3310")

  val phoneNumberJson: JsValue = Json.parse(
    """
      |{"value":"0300 200 3310"}
      |""".stripMargin)

  val emailModel: Email = Email("test@digital.hmrc.gov.uk")

  val emailJson: JsValue = Json.parse(
    """
      |{"value":"test@digital.hmrc.gov.uk"}
      |""".stripMargin)

  val ninoModel: Nino = Nino("AA123456A")

  val ninoJson: JsValue = Json.parse(
    """
      |{"nino":"AA123456A"}
      |""".stripMargin)

}
