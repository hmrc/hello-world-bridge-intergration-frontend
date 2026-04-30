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

import models.UserAnswers
import org.apache.pekko.event.Logging
import pages.QuestionPage
import play.api.Logging
import play.api.libs.json.{Reads, Writes}

import scala.util.{Failure, Success}

trait ServiceHelper extends Logging {
   def setIfEmpty[A](
                             answers: UserAnswers,
                             page: QuestionPage[A],
                             value: Option[A]
                           )(implicit reads: Reads[A], writes: Writes[A]): UserAnswers =
    (answers.get(page), value) match {

      case (None, Some(v)) =>
        answers.set(page, v) match {
          case Success(updated) => updated
          case Failure(e) =>
            logger.warn(s"Failed to auto-populate $page from property", e)
            answers
        }

      case _ =>
        answers
    }
}
