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

import models.properties.VMVProperty

import javax.inject.{Inject, Singleton}

@Singleton
class SortingVMVPropertiesService @Inject()() {

  def sort(properties: List[VMVProperty], sortBy: String): List[VMVProperty] = {

    def safeDescription(p: VMVProperty): String =
      p.valuations.lastOption
        .map(_.descriptionText.toLowerCase)
        .getOrElse("")

    def safeRateableValue(p: VMVProperty): Long =
      p.valuations.lastOption
        .flatMap(_.rateableValue.map(_.longValue))
        .getOrElse(0L)

    sortBy match {

      case "AddressASC" =>
        properties.sortBy(_.addressFull.toLowerCase)

      case "AddressDESC" =>
        properties.sortBy(_.addressFull.toLowerCase)(Ordering[String].reverse)

      case "DescriptionASC" =>
        properties.sortBy(safeDescription)

      case "DescriptionDESC" =>
        properties.sortBy(safeDescription)(Ordering[String].reverse)

      case "ReferenceASC" =>
        properties.sortBy(_.localAuthorityReference.toLowerCase)

      case "ReferenceDESC" =>
        properties.sortBy(_.localAuthorityReference.toLowerCase)(Ordering[String].reverse)

      case "RateableValueASC" =>
        properties.sortBy(safeRateableValue)

      case "RateableValueDESC" =>
        properties.sortBy(safeRateableValue)(Ordering[Long].reverse)

      case _ =>
        // Default sort: Address A → Z
        properties.sortBy(_.addressFull.toLowerCase)
    }
  }
}

