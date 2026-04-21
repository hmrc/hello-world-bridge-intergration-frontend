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

package service

import models.assessment.AssessmentProperty

import javax.inject.{Inject, Singleton}

@Singleton
class AssessmentPropertiesSortingService @Inject()() {

  def sort(list: List[AssessmentProperty], sortBy: String): List[AssessmentProperty] =
    sortBy match {

      case "AddressASC" =>
        list.sortBy(_.address)

      case "AddressDESC" =>
        list.sortBy(_.address)(Ordering[String].reverse)

      case "ForeignIdASC" =>
        list.sortBy(_.foreignId)

      case "ForeignIdDESC" =>
        list.sortBy(_.foreignId)(Ordering[String].reverse)

      case "DescriptionASC" =>
        list.sortBy(_.description)

      case "DescriptionDESC" =>
        list.sortBy(_.description)(Ordering[String].reverse)

      case "RateableValueASC" =>
        list.sortBy(_.rateableValue)

      case "RateableValueDESC" =>
        list.sortBy(_.rateableValue)(Ordering[Int].reverse)

      case _ =>
        list.sortBy(_.address)
    }
}

