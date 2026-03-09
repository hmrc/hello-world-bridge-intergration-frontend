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

package models.bridge.common

package models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class CommonModelsSpec extends AnyWordSpec with Matchers {

  "CodeMeaning" should {
    "serialise and deserialise correctly" in {
      val cm = CodeMeaning(Some("A1"), Some("Example"))
      val json = Json.toJson(cm)

      (json \ "code").as[String] shouldBe "A1"
      (json \ "meaning").as[String] shouldBe "Example"

      json.as[CodeMeaning] shouldBe cm
    }
  }

  "ForeignId" should {
    "serialise and deserialise correctly" in {
      val fid = ForeignId("SYS", "LOC", "12345")
      val json = Json.toJson(fid)

      (json \ "system").as[String] shouldBe "SYS"
      (json \ "location").as[String] shouldBe "LOC"
      (json \ "value").as[String] shouldBe "12345"

      json.as[ForeignId] shouldBe fid
    }
  }

  "ProtoData" should {
    "serialise and deserialise correctly" in {
      val pd = ProtoData("application/pdf", "Document", is_pointer = false, "", "dGVzdA==")
      val json = Json.toJson(pd)

      (json \ "mime_type").as[String] shouldBe "application/pdf"
      (json \ "label").as[String] shouldBe "Document"
      (json \ "is_pointer").as[Boolean] shouldBe false

      json.as[ProtoData] shouldBe pd
    }
  }

  "MetadataStage" should {
    "apply default empty maps when fields are missing" in {
      val json = Json.parse("""{ }""")
      val ms = json.as[MetadataStage]

      ms.selecting shouldBe empty
      ms.filtering shouldBe empty
    }

    "serialise and deserialise full object correctly" in {
      val ms = MetadataStage(
        selecting = Map("a" -> "1"),
        filtering = Map("b" -> "2"),
        supplementing = Map("c" -> "3")
      )

      val json = Json.toJson(ms)
      json.as[MetadataStage] shouldBe ms
    }
  }

  "SendingMetadata" should {
    "serialise and deserialise correctly" in {
      val stage = MetadataStage(selecting = Map("x" -> "y"))
      val sm = SendingMetadata(stage, stage, stage)

      val json = Json.toJson(sm)
      json.as[SendingMetadata] shouldBe sm
    }
  }

  "ReceivingMetadata" should {
    "serialise and deserialise correctly" in {
      val stage = MetadataStage(filtering = Map("k" -> "v"))
      val rm = ReceivingMetadata(stage, stage, stage)

      val json = Json.toJson(rm)
      json.as[ReceivingMetadata] shouldBe rm
    }
  }

  "Metadata" should {
    "serialise and deserialise nested structure correctly" in {
      val stage1 = MetadataStage(selecting = Map("s1" -> "v1"))
      val stage2 = MetadataStage(selecting = Map("s2" -> "v2"))

      val sm = SendingMetadata(stage1, stage1, stage1)
      val rm = ReceivingMetadata(stage2, stage2, stage2)

      val metadata = Metadata(sm, rm)

      val json = Json.toJson(metadata)

      json.as[Metadata] shouldBe metadata
    }
  }
}
