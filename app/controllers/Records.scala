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
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toContraFunctorOps
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller

object Records extends Controller {
  import play.api.Play.current
  
  implicit val writes = Json.writes[Record]
  
  val recordForm = Form(
    mapping(
      "id" -> optional(number),
      "domainId" -> number,
      "name" -> nonEmptyText,
      "recordType" -> nonEmptyText,
      "content" -> nonEmptyText,
      "ttl" -> number,
      "priority" -> number,
      "changeDate" -> number,
      "accountid" -> number
    ) (Record.apply)(Record.unapply)
  )
  
  def index = Action { implicit request =>
    Ok(views.html.records.index("Records List"))
  }
  
  def search = Action {
    Ok
  }
  
  def listAll = Action { implicit request =>
    val json = Json.toJson(models.Records.findAll.map(r => Json.toJson(r)))
    Logger.debug(s"${request.method} ${request.path} -> Records.listAll")
    Ok(json) as JSON
  }
  
  def get(id: Int) = Action { implicit request =>
    models.Records.findById(id) match {
      case Success(r) => {
        Ok(Json.toJson(r))
      }
      case Failure(e) => NotFound
    }
  }
  
  def create = Action { implicit request =>
    implicit val writes = Json.writes[Record]
    implicit val reads = Json.reads[Record]
    request.body.asJson.map { json =>
      json.validate[Record].fold(
        invalid => {
          BadRequest(Json.toJson(Map("error" -> invalid.head.toString)))
        },
        valid => {
          models.Records.insert(valid) match {
            case Success(r) => {
              Ok(Json.toJson(r))
            }
            case Failure(e) => BadRequest(Json.toJson(Map("error" -> e.getMessage)))
          }
        })
    }.getOrElse(BadRequest (Json.toJson(Map("status" -> "error request.body", "message" -> "Content Type Not Json"))))
  }
  
  def updateAll = Action { implicit request =>
    val json = Json.arr("PUT /records -> Records.updateAll UNIMPLEMENTED")
    Ok(json) as JSON
  }
  
  def deleteAll = Action { implicit request =>
    val count = models.Records.deleteAll
    Logger.debug(s"deleteAll :: deleted all records, count=$count")
    val json = Json.arr()
    Ok(json) as JSON
  }
  
  def updateIfExists(id: Int) = Action { implicit request =>
    implicit val writes = Json.writes[Record]
    implicit val reads = Json.reads[Record]
    request.body.asJson.map { json =>
      json.validate[Record].fold(
        invalid => {
          BadRequest(Json.toJson(Map("error" -> invalid.head.toString)))
        },
        valid => {
          models.Records.update(id, valid) match {
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
  
  def deleteOne(id: Int) = Action { implicit request =>
    val deleted = models.Records.delete(id)
    val json = Json.arr(s"DELETE /records/$id -> Records.deleteOne $id")
    Ok(json) as JSON
  }

  // VIEWS SECTION FOR TEMPORARY VIEWS

  def list(page: Int, orderBy: Int) = Action { implicit request =>
    val records = models.Records.findPage(page, orderBy)
    Ok(views.html.records.list(records.items))
  }
  
  def show(id: Int) = Action { implicit request =>
    models.Records.findById(id) match {
      case Success(r) => {
        Ok(views.html.records.show(r.getOrElse(null)))
      }
      case Failure(e) => NotFound
    }
  }
  
  def getRecordCount(id: Int) : Int =  {
    models.Records.findByAccountId(id) match {
      case Success(r) => r.length
      case Failure(r) => 0
    }
  }
  
  def listForAccountId(id: Int) = Action { implicit request =>
    models.Records.findByAccountId(id) match {
      case Success(r) => {
        Ok(views.html.records.list(r))
      }
      case Failure(r) => NotFound
    }
  }
  
  def listAllAccountIds() = Action { implicit request =>
    Ok(views.html.accounts.list(models.Records.listAccountIds))
  }
  
  def listByDomainId(id: Int) = Action { implicit request =>
    models.Records.findByDomainId(id) match {
      case Success(r) => {
        Ok(views.html.records.list(r))
      }
      case Failure(r) => NotFound
    }
  }
  
  def newRecord() = Action { implicit request =>
    Ok(views.html.records.create(recordForm))
  }
}