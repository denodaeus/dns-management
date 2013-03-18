package controllers

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import models.Supermaster
import models.SupermasterId
import models.SupermasterForCreate
import models.SupermasterForUpdate

import play.Logger
import play.api.data.Form
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

object Supermasters extends Controller {
  import play.api.Play.current
  
  implicit val writes = Json.writes[Supermaster]
  
  val supermasterForm = Form(
    mapping(
      "id" -> optional(number),
      "ip" -> nonEmptyText,
      "nameServer" -> nonEmptyText,
      "account" -> nonEmptyText
    ) (Supermaster.apply)(Supermaster.unapply)
  )
  
  def index = Action {
    Ok(views.html.supermasters.index("Supermasters List"))
  }
  
  def listAll = Action { implicit request =>
    val json = Json.toJson(Supermaster.findAll.map(s => Json.toJson(s)))
    Logger.debug(s"${request.method} ${request.path} -> Supermasters.listAll")
    Ok(json) as JSON
  }
  
  def get(id: Int) = Action { implicit request =>
    Supermaster.findById(id) match {
      case Success(r) => {
        Ok(Json.toJson(r))
      }
      case Failure(e) => NotFound
    }
  }
  
  def create = Action { implicit request =>
    implicit val writes = Json.writes[SupermasterId]
    implicit val reads = Json.reads[SupermasterForCreate]
    request.body.asJson.map { json =>
      json.validate[SupermasterForCreate].fold(
        invalid => {
          BadRequest(Json.toJson(Map("error" -> invalid.head.toString)))
        },
        valid => {
          Supermaster.create(valid) match {
            case Success(r) => {
              Ok(Json.toJson(r))
            }
            case Failure(e) => BadRequest(Json.toJson(Map("error" -> e.getMessage)))
          }
        })
    }.getOrElse(BadRequest (Json.toJson(Map("status" -> "error request.body", "message" -> "Content Type Not Json"))))
  }
  
  def updateAll = Action { implicit request =>
    val json = Json.arr("PUT /supermaster -> Supermaster.updateAll UNIMPLEMENTED")
    Ok(json) as JSON
  }
  
  def deleteAll = Action { implicit request =>
    val count = Supermaster.deleteAll
    Logger.debug(s"deleteAll :: deleted all supermasters, count=$count")
    val json = Json.arr()
    Ok(json) as JSON
  }
  
  def updateIfExists(id: Int) = Action { implicit request =>
    implicit val writes = Json.writes[SupermasterId]
    implicit val reads = Json.reads[SupermasterForUpdate]
    request.body.asJson.map { json =>
      json.validate[SupermasterForUpdate].fold(
        invalid => {
          BadRequest(Json.toJson(Map("error" -> invalid.head.toString)))
        },
        valid => {
          Supermaster.update(valid) match {
            case Success(r) => {
              Ok (Json.toJson(SupermasterId(valid.id)))
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
  
  def deleteOne(id: Int) = Action { implicit request =>
    val deleted = Supermaster.delete(id)
    val json = Json.arr(s"DELETE /records/$id -> Records.deleteOne $id")
    Ok(json) as JSON
  }

}