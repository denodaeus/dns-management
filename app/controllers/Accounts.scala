package controllers

import models.Account
import models.Record

import scala.util.Try
import scala.util.Success
import scala.util.Failure

import play.Logger
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.data.Forms.list
import play.api.data.Forms.text
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toContraFunctorOps
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller

object Accounts extends Controller with Secured {
  import play.api.Play.current
  
  implicit val recordWrites = Json.writes[Record]
  implicit val writes = Json.writes[Account]
    
/*  val accountForm = Form(
    mapping(
      "id" -> optional(number),
      "records" -> list(text)
    )
  )(Account.apply)(Account.unapply)*/
  
  def get(id: Int) = Action { implicit request =>
    models.Accounts.findById(id) match {
      case Success(a) => Ok(Json.toJson(a))
      case Failure(a) => {Logger.debug("Failed to get account " + id + " because " + a.getMessage()); NotFound}
    }
  }
  
  // VIEWS SECTION FOR TEMPORARY VIEWS
  
    def show(id: Int) = withAuth { username => implicit request =>
    models.Accounts.findById(id) match {
      case Success(a) => Ok(views.html.accounts.show(id, a))
      case Failure(e) => NotFound
    }
  }

}