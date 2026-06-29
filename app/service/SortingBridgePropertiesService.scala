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

import models.bridge.search.RecordWrapper
import javax.inject.{Inject, Singleton}

import scala.util.Try

@Singleton
class SortingBridgePropertiesService @Inject()() {

  def sort(properties: Seq[RecordWrapper], sortBy: String): Seq[RecordWrapper] = {
    def address(p: RecordWrapper): String =
      p.record.data.list_entry.relevant_property.full_address.toLowerCase

    def description(p: RecordWrapper): String =
      p.record.data.list_entry.use.description.getOrElse("").toLowerCase

    def reference(p: RecordWrapper): String =
      p.record.data.list_entry.administration.collection_authority_ref.getOrElse("").toLowerCase

    def rateableValue(p: RecordWrapper): BigDecimal =
      Try(BigDecimal(p.record.data.list_entry.valuation.value.replace(",", "")))
        .getOrElse(BigDecimal(0))

    sortBy match {
      case "AddressASC" =>
        properties.sortBy(address)

      case "AddressDESC" =>
        properties.sortBy(address)(Ordering[String].reverse)

      case "DescriptionASC" =>
        properties.sortBy(description)

      case "DescriptionDESC" =>
        properties.sortBy(description)(Ordering[String].reverse)

      case "ReferenceASC" =>
        properties.sortBy(reference)

      case "ReferenceDESC" =>
        properties.sortBy(reference)(Ordering[String].reverse)

      case "RateableValueASC" =>
        properties.sortBy(rateableValue)

      case "RateableValueDESC" =>
        properties.sortBy(rateableValue)(Ordering[BigDecimal].reverse)

      case _ =>
        properties.sortBy(address)
    }
  }
}