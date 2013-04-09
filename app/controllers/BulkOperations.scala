package controllers

import scala.util.Failure
import scala.util.Success

import models.Record

import play.Logger
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.mvc.Action
import play.api.mvc.Controller

object BulkOperations extends Controller with Secured {
  
  def index = withAuth { username => implicit request =>
    Ok
  }
  
  def migrate() = withAuth { username => implicit request =>
    Ok(views.html.accounts.bulkmigrate())
  }
  
  def bulkMigrate = withAuth { username => implicit request =>
    Ok
  }

}