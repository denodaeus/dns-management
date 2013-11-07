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
  
  def updateIfExists(id: Int) = Action { implicit request =>
    implicit val writes = Json.writes[Server]
    implicit val reads = Json.reads[Server]
    request.body.asJson.map { json =>
      json.validate[Server].fold(
        invalid => {
          BadRequest(Json.toJson(Map("error" -> invalid.head.toString)))
        },
        valid => {
          models.Servers.update(id, valid) match {
            case Success(r) => {
              Ok (Json.toJson(r))
            }
            case Failure(e) => {
              Logger.error(s"updateIfExists :: error updating record $id, cause=", e)
              BadRequest(Json.toJson(Map("error" -> e.getMessage)))
            }
          }
        }
      )
    }.getOrElse(BadRequest (Json.toJson(Map("status" -> "error request.body", "message" -> "Content Type Not Json"))))
  }
  
  // VIEWS SECTION FOR TEMPORARY VIEWS
  
  def list(page: Int, orderBy: Int) = withAuth { username =>
    implicit request =>
      val servers = models.Servers.findPage(page, orderBy)
      Ok(views.html.servers.list(servers.items))
  }
  
  def create() = withAuth { username =>
    implicit request =>
    Ok(views.html.servers.create(serverForm))
  }
  
  def createServer() = withAuth { username => implicit request =>
  	serverForm.bindFromRequest.fold(
  	  formWithErrors => BadRequest(views.html.servers.create(formWithErrors)).flashing("error" -> s"Errors on submission: ${formWithErrors.errorsAsJson}"),
  	  server => {
  	    models.Servers.insert(server) match {
  	      case Success(r) => Redirect(routes.Servers.show(r)).flashing("success" -> s"Successfully inserted server $r")
  	      case Failure(r) => BadRequest(views.html.servers.create(serverForm)).flashing("error" -> s"error inserting server $r, error=> ${r.printStackTrace()}")
  	    }
  	  }
  	)
  }
  
  def show(id: Int) = withAuth { username => implicit request =>
    models.Servers.findById(id) match {
      case Success(s) => {
        Logger.debug(s"show :: found for id=$id")
        Ok(views.html.servers.show(s.getOrElse(null)))
      }
      case Failure(e) => { Logger.debug(s"show :: id=$id NotFound"); NotFound }
    }
  }

}