package controllers

import scala.util.Failure
import scala.util.Success

import models.Server
import play.Logger
import play.api.data.Form
import play.api.data.Forms.longNumber
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toContraFunctorOps
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller

object Servers extends Controller with Secured {
  import play.api.Play.current
  
  implicit val writes = Json.writes[Server]
  
  val serverForm = Form(
    mapping(
      "id" -> optional(number),
      "hostName" -> nonEmptyText,
      "ip" -> nonEmptyText
    )(Server.apply)(Server.unapply)
  )
  
  def listAll = Action { implicit request => 
    val json = Json.toJson(models.Servers.findAll.map(s => Json.toJson(s)))
    Ok(json) as JSON
  }
  
  // VIEWS SECTION FOR TEMPORARY VIEWS
  
  def list(page: Int, orderBy: Int) = withAuth { username =>
    implicit request =>
      val servers = models.Servers.findPage(page, orderBy)
      Ok(views.html.servers.list(servers.items))
  }

}