package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.User

object Authentication extends Controller {

    val loginForm = Form(
      tuple(
        "username" -> text,
        "password" -> text
      ) verifying ("Invalid login", result => result match {
        case (username, password) => check(username, password)
      })
  )
  
  def check(username: String, password: String) = {
    User.canAccessSupportSite(username, password)
  }
  
  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }
  
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      user => Redirect(routes.Domains.index).withSession(Security.username -> user._1)
    )
  }
  
  def logout = Action { implicit request => 
    Ok(views.html.login(loginForm))
    Redirect(routes.Authentication.login).withNewSession.flashing(
      "success" -> "You are now logged out"
    )
  }
}