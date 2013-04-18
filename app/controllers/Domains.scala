package controllers

import scala.util.Failure
import scala.util.Success

import models.Domain
import play.Logger
import play.api.data.Form
import play.api.data.Forms.longNumber
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toContraFunctorOps
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller

object Domains extends Controller with Secured {
  import play.api.Play.current
  
  implicit val writes = Json.writes[Domain]
  
  val domainForm = Form(
    mapping(
      "id" -> optional(number),
      "name" -> nonEmptyText,
      "master" -> optional(text),
      "lastCheck" -> optional(longNumber),
      "domainType" -> nonEmptyText,
      "notifiedSerial" -> longNumber,
      "account" -> optional(nonEmptyText)
    ) (Domain.apply)(Domain.unapply)
  )
  
  def index = withAuth { username => implicit request =>
    Ok(views.html.domains.index("Domains"))
  }
  
  def listAll = Action { implicit request =>
    val json = Json.toJson(models.Domains.findAll.map(d => Json.toJson(d)))
    Ok(json) as JSON
  }
  
  def get(id: Int) = Action { implicit request =>
    models.Domains.findById(id) match {
      case Success(d) => {
        Ok(Json.toJson(d))
      }
      case Failure(e) => NotFound
    }
  }
  
  def create = Action { implicit request =>
    implicit val writes = Json.writes[Domain]
    implicit val reads = Json.reads[Domain]
    request.body.asJson.map { json =>
      json.validate[Domain].fold(
        invalid => {
          BadRequest(Json.toJson(Map("error" -> invalid.head.toString)))
        },
        valid => {
          models.Domains.insert(valid) match {
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
  
  def updateIfExists(id: Int) = Action { implicit request =>
    implicit val writes = Json.writes[Domain]
    implicit val reads = Json.reads[Domain]
    request.body.asJson.map { json =>
      json.validate[Domain].fold(
        invalid => {
          BadRequest(Json.toJson(Map("error" -> invalid.head.toString)))
        },
        valid => {
          models.Domains.update(id, valid) match {
            case Success(d) => {
              Ok (Json.toJson(d))
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
    val deleted = models.Domains.delete(id)
    val json = Json.arr(s"DELETE /domain/$id -> Domain.deleteOne $id")
    Ok(json) as JSON
  }


  // VIEWS SECTION FOR TEMPORARY VIEWS

  def list(page: Int, orderBy: Int) = withAuth { username => implicit request =>
    val records = models.Domains.findPage(page, orderBy)
    Ok(views.html.domains.list(records.items))
  }

  def show(id: Int) = withAuth { username => implicit request =>
    models.Domains.findById(id) match {
      case Success(d) => {
        Ok(views.html.domains.show(d.getOrElse(null)))
      }
      case Failure(e) => NotFound
    }
  }
  
  def newDomain() = withAuth { username => implicit request =>
    Ok(views.html.domains.create(domainForm))
  }

}  