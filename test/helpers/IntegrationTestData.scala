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

import models.registration.*

import java.time.LocalDate

trait IntegrationTestData {

  val sampleRatepayerRegistration: RegisterRatepayer = RegisterRatepayer(
    userType = Some(RatepayerType.Individual),
    agentStatus = Some(AgentStatus.Agent),
    name = Some(Name("Jane Doe")),
    tradingName = Some(TradingName("Jane's Bakery")),
    email = Some(Email("jane.doe@example.com")),
    nino = Some(Nino("AB123456C")),
    contactNumber = Some(PhoneNumber("07123456789")),
    secondaryNumber = Some(PhoneNumber("07987654321")),
    address = Some(Address("1 High Street", None, "London", None, Postcode("SW1A 1AA"))),
    trnReferenceNumber = Some(TRNReferenceNumber(ReferenceType.Nino, "TRN123456")),
    isRegistered = Some(true)
  )
  
  val cidMatchingDetailsResponseJson: String =
    """
      |{
      |  "name": {
      |    "current": {
      |      "firstName": "Jim",
      |      "lastName": "Ferguson"
      |    },
      |    "previous": []
      |  },
      |  "ids": {
      |    "sautr": "1097133333",
      |    "nino": "AA000003D"
      |  },
      |  "dateOfBirth": "23041948",
      |  "deceased": false
      |}
      |""".stripMargin

  val cidPersonDetailsResponseJson: String =
    """
      |{
      |  "etag" : "115",
      |  "person" : {
      |    "firstName" : "John",
      |    "middleName" : "Joe",
      |    "lastName" : "Ferguson",
      |    "title" : "Mr",
      |    "honours": "BSC",
      |    "sex" : "M",
      |    "dateOfBirth" : "1952-04-01",
      |    "nino" : "TW189213B",
      |    "deceased" : false
      |  },
      |  "address" : {
      |    "line1" : "26 FARADAY DRIVE",
      |    "line2" : "PO BOX 45",
      |    "line3" : "LONDON",
      |    "postcode" : "CT1 1RQ",
      |    "startDate": "2009-08-29",
      |    "country" : "GREAT BRITAIN",
      |    "type" : "Residential"
      |  }
      |}
      |""".stripMargin

  
  

  val addressLookupResponseJson : String =
    """
      |[
      | {
      |   "id":"ID1",
      |   "uprn":1,
      |   "address":{
      |     "lines":["Unit 13 Trident Industrial Estate Blackthorn"],
      |     "town":"Colnbrook",
      |     "county":"Slough",
      |     "postcode":"SL3 0AX",
      |     "subdivision":{"code":"GB-ENG","name":"England"},
      |     "country":{"code":"GB","name":"United Kingdom"}
      |   },
      |   "language":"English",
      |   "location":["1000","2000"]
      | },
      |  {
      |   "id":"ID1",
      |   "uprn":1,
      |   "address":{
      |     "lines":["40 Manor Road"],
      |     "town":"Dawley",
      |     "county":"Telford",
      |     "postcode":"TF4 3ED",
      |     "subdivision":{"code":"GB-ENG","name":"England"},
      |     "country":{"code":"GB","name":"United Kingdom"}
      |   },
      |   "language":"English",
      |   "location":["1000","2000"]
      | }
      |]
      |""".stripMargin



  val invalidAddressLookupResponseJson : String =
    """
      |{
      |  "parentUprn": 1234567890,
      |  "usrn": 987654321,
      |  "organisation": "Capgemini",
      |  "address": {
      |    "lines": [
      |      "99Wibble Rd"
      |    ],
      |    "town": "Worthing",
      |    "postcode": "BN110AA",
      |    "subdivision": {
      |      "code": "code",
      |      "name": "name"
      |    },
      |    "country": {
      |      "code": "GB",
      |      "name": "Great Britain"
      |    }
      |  },
      |  "localCustodian": {
      |    "code": 123,
      |    "name": "LcName"
      |  },
      |  "language": "English",
      |  "administrativeArea": "AdminArea",
      |  "poBox": "PO321"
      |}
      |""".stripMargin

  val gnapToken = "i16lTUCYVVcwAEDOtbZNyly2wwgJ"

  val tokenAttributesResponseJson: String =
    """{
      | "authenticationProvider": "One Login",
      | "name": "John Ferguson",
      | "email": "test@testUser.com",
      | "identity": {
      |    "provider": "MDTP",
      |    "level": "50",
      |    "nino": "AB666666A"
      | },
      | "enrolments": [{
      |			"service": "IR-SA",
      |			"identifiers": [{
      |				"key": "UTR",
      |				"value": "1234567890"
      |			}],
      |   "state": "Activated",
      |			"friendlyName": "My SA"
      |		}],
      | "credId": "12345",
      | "eacdGroupId": "12345",
      | "caUserId": "12345"
      |}""".stripMargin


}
