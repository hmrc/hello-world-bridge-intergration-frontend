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

package controllers

import forms.UserNameForm
import models.UserName
import play.api.mvc.*
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.UserNameView

import javax.inject.Inject

class UserNameController @Inject()(
                                    val controllerComponents: MessagesControllerComponents,
                                    view: UserNameView
                                  ) extends FrontendBaseController {

  def onPageLoad(): Action[AnyContent] = Action { implicit request =>
    Ok(view(UserNameForm.form))
  }

  def onSubmit(): Action[AnyContent] = Action { implicit request =>
    UserNameForm.form.bindFromRequest().fold(
      formWithErrors => BadRequest(view(formWithErrors)),
      nameValue => {
        val userName = UserName(nameValue)
        
        // TODO:  store the data ???

        Ok(s"User name: ${userName.name}")
      }
    )
  }
}


