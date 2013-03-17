package controllers

import scala.util.Failure
import scala.util.Success

import models.Domain
import models.DomainForCreate
import models.DomainForUpdate
import models.DomainId
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

object Domains extends Controller {
  import play.api.Play.current
  
  implicit val writes = Json.writes[Domain]
  
  val domainForm = Form(
    mapping(
      "id" -> optional(number),
      "name" -> nonEmptyText,
      "master" -> nonEmptyText,
      "lastCheck" -> longNumber,
      "domainType" -> nonEmptyText,
      "notifiedSerial" -> longNumber,
      "account" -> nonEmptyText
    ) (Domain.apply)(Domain.unapply)
  )
  
  def listAll = Action { implicit request =>
    val json = Json.toJson(Domain.findAll.map(d => Json.toJson(d)))
    Ok(json) as JSON
  }
  
  def get(id: Int) = Action { implicit request =>
    Domain.findById(id) match {
      case Success(d) => {
        Ok(Json.toJson(d))
      }
      case Failure(e) => NotFound
    }
  }
  
  def create = Action { implicit request =>
    implicit val writes = Json.writes[DomainId]
    implicit val reads = Json.reads[DomainForCreate]
    request.body.asJson.map { json =>
      json.validate[DomainForCreate].fold(
        invalid => {
          BadRequest(Json.toJson(Map("error" -> invalid.head.toString)))
        },
        valid => {
          Domain.create(valid) match {
            case Success(r) => {
              Ok(Json.toJson(r))
            }
            case Failure(e) => BadRequest(Json.toJson(Map("error" -> e.getMessage)))
          }
        })
    }.getOrElse(BadRequest (Json.toJson(Map("status" -> "error request.body", "message" -> "Content Type Not Json"))))
  }
  
  def updateAll = Action { implicit request =>
    val json = Json.arr("PUT /domains -> Domains.updateAll UNIMPLEMENTED")
    Ok(json) as JSON
  }
  
  def deleteAll = Action { implicit request =>
    val count = Domain.deleteAll
    Logger.debug(s"deleteAll :: deleted all records, count=$count")
    val json = Json.arr()
    Ok(json) as JSON
  }
  
  def updateIfExists(id: Int) = Action { implicit request =>
    implicit val writes = Json.writes[DomainId]
    implicit val reads = Json.reads[DomainForUpdate]
    request.body.asJson.map { json =>
      json.validate[DomainForUpdate].fold(
        invalid => {
          BadRequest(Json.toJson(Map("error" -> invalid.head.toString)))
        },
        valid => {
          Domain.update(valid) match {
            case Success(r) => {
              Ok (Json.toJson(DomainId(valid.id)))
            }
            case Failure(e) => {
              Logger.error(s"updateIfExists :: error updating domain $id, cause=", e)
              BadRequest(Json.toJson(Map("error" -> e.getMessage)))
            }
          }
        }
      )
    }.getOrElse(BadRequest (Json.toJson(Map("status" -> "error request.body", "message" -> "Content Type Not Json"))))
  }
  
  def deleteOne(id: Int) = Action { implicit request =>
    val deleted = Domain.delete(id)
    val json = Json.arr(s"DELETE /domain/$id -> Domain.deleteOne $id")
    Ok(json) as JSON
  }
}  