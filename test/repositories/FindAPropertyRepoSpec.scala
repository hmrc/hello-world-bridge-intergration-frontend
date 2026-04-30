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

package repositories

import config.FrontendAppConfig
import models.properties.{StoredVMVProperties, VMVProperties}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.mongodb.scala.*
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.ReplaceOptions
import org.mongodb.scala.result.UpdateResult
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.mongo.MongoComponent

import scala.concurrent.{ExecutionContext, Future}

class FindAPropertyRepoSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with MockitoSugar {

  implicit val ec: ExecutionContext = ExecutionContext.global

  private val app =
    new GuiceApplicationBuilder()
      .configure(
        "mongodb.uri" -> "mongodb://localhost:27017/test",
        "mongodb.db"  -> "test",
        "timeToLive"  -> "24"
      )
      .build()

  private val mongoComponent =
    app.injector.instanceOf[MongoComponent]

  private val config =
    app.injector.instanceOf[FrontendAppConfig]

  private val mockCollection =
    mock[MongoCollection[StoredVMVProperties]]

  private val repo = new FindAPropertyRepo(mongoComponent, config) {

    override lazy val collection: MongoCollection[StoredVMVProperties] =
      mockCollection

    override def ensureIndexes(): Future[Seq[String]] =
      Future.successful(Seq.empty)
  }

  "FindAPropertyRepo.upsert" should {

    "return true when Mongo acknowledges the upsert" in {

      val updateResult = mock[UpdateResult]
      when(updateResult.wasAcknowledged()).thenReturn(true)

      when(
        mockCollection.replaceOne(
          any[Bson],
          any[StoredVMVProperties],
          any[ReplaceOptions]
        )
      ).thenReturn(SingleObservable(updateResult)) // ✅ FIX

      repo.upsert("user-1", VMVProperties(1, Nil)).futureValue mustBe true
    }

    "fail with IllegalStateException when Mongo fails" in {

      val failedObservable = mock[org.mongodb.scala.SingleObservable[UpdateResult]]

      when(failedObservable.toFuture())
        .thenReturn(Future.failed(new RuntimeException("mongo down")))

      when(
        mockCollection.replaceOne(
          any[Bson],
          any[StoredVMVProperties],
          any[ReplaceOptions]
        )
      ).thenReturn(failedObservable)

      val ex =
        repo.upsert("user-1", VMVProperties(1, Nil)).failed.futureValue

      ex mustBe a[IllegalStateException]
      ex.getMessage must include("VMV properties have not been inserted")
    }
  }
}