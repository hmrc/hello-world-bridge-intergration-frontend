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

package utils

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class UniqueIdGeneratorSpec extends AnyWordSpec with Matchers {
  "UniqueIdGenerator.generateId" should {
    "return a formatted reference of the correct length" in {
      val id = UniqueIdGenerator.generateId
      id.count(_ == '-') shouldBe 2
    }
    
    "only contain allowed characters and hyphens" in {
      val id = UniqueIdGenerator.generateId
      val raw = UniqueIdGenerator.parse(id)
      raw.forall(UniqueIdGenerator.allowedChars.contains(_)) shouldBe true
    }
  }
  
  "UniqueIdGenerator.validateId" should {
    "accept a valid unformatted reference and return it formatted" in {
      val raw = "ABCDEFGHJKLM"
      val result = UniqueIdGenerator.validateId(raw)
      
      result shouldBe Right("ABCD-EFGH-JKLM")
    }
    
    "accept a valid formatted reference in any case" in {
      val formatted = "abcd-efgh-jklm"
      UniqueIdGenerator.validateId(formatted) shouldBe Right("ABCD-EFGH-JKLM")
    }
    
    "accept references containing spaces and hyphens" in {
      val messy = "ABCD EFGH-JKLM"
      UniqueIdGenerator.validateId(messy) shouldBe Right("ABCD-EFGH-JKLM")
    }
    
    "reject references with invalid characters" in {
      val invalid = "ABCD-EFGH-IJKL" //I is not allowed
      UniqueIdGenerator.validateId(invalid).isLeft shouldBe true
    }
    
    "reject references of the wrong length" in {
      val tooShort = "ABCDE"
      UniqueIdGenerator.validateId(tooShort).isLeft shouldBe true
    }
  }
  
  "UniqueIdGenerator.format" should {
    "group a raw reference into 4-character chunks separated by hyphens" in {
      UniqueIdGenerator.format("ABCDEFGHJKLM") shouldBe "ABCD-EFGH-JKLM"
    }
  }
  
  "UniqueIdGenerator.parse" should {
    "remove hyphens and normalise to upper case" in {
      UniqueIdGenerator.parse("abcd-efgh-jklm") shouldBe "ABCDEFGHJKLM"
    }
  }
}