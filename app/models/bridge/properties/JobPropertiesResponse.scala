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

package models.properties



import play.api.libs.json.{Json, OFormat, Reads, __}

import play.api.libs.functional.syntax._

import models.bridge.property.Property

import models.bridge.person.Person

import models.bridge.relationhship.Relationship



final case class JobPropertiesResponse(job: Job)

object JobPropertiesResponse {

  implicit val format: OFormat[JobPropertiesResponse] = Json.format[JobPropertiesResponse]

}



final case class Job(compartments: Compartments)

object Job {

  // Reads only the compartments and ignores any other "job" fields the API includes

  implicit val reads: Reads[Job] =

    (__ \ "compartments").read[Compartments].map(Job.apply)



  implicit val format: OFormat[Job] = {

    // format needs both reads and writes; writes can be derived

    OFormat(reads, Json.writes[Job])

  }

}



final case class Compartments(

                               properties: List[Property],

                               persons: List[Person],

                               relationships: List[Relationship]

                             )

object Compartments {

  // Reads only these three arrays and ignores any other compartments (e.g. processes)

  implicit val reads: Reads[Compartments] = (

    (__ \ "properties").read[List[Property]] and

      (__ \ "persons").read[List[Person]] and

      (__ \ "relationships").read[List[Relationship]]

    )(Compartments.apply)



  implicit val format: OFormat[Compartments] =

    OFormat(reads, Json.writes[Compartments])

}

 