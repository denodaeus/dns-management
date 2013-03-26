package controllers

import play.api._
import play.api.mvc._
import models.Domain

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.domains.list(Domain.findAll))
  }
  
}