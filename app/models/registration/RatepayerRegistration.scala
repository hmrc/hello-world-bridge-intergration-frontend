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

package models.registration

import play.api.libs.json.{Format, Json}

final case class RatepayerRegistration(userType: Option[UserType] = None,
                                       agentStatus: Option[AgentStatus] = None,
                                       name: Option[Name] = None,
                                       tradingName: Option[TradingName] = None,
                                       email: Option[Email] = None,
                                       nino: Option[String] = None,
                                       contactNumber: Option[PhoneNumber] = None,
                                       secondaryNumber: Option[PhoneNumber] = None,
                                       address: Option[Address] = None,
                                       trnReferenceNumber: Option[TRNReferenceNumber] = None,
                                       isRegistered: Option[Boolean] = Some(false),
                                       recoveryId: Option[String] = None
                                 )

object RatepayerRegistration {

  implicit val format: Format[RatepayerRegistration] = Json.format[RatepayerRegistration]

}
