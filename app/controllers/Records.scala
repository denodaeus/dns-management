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

object Records extends Controller with Secured {
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
      "accountId" -> number
    ) (Record.apply)(Record.unapply)
  )
  
  def index = withAuth { username => implicit request =>
    Ok(views.html.records.index("Records List"))
  }
  
  def search = withAuth { username => implicit request =>
    Ok
  }
  
  def listAll(page: Int, orderBy: Int) = Action { implicit request =>
    val records = Json.toJson(models.Records.findPage(page, orderBy).items)
    Logger.debug(s"${request.method} ${request.path} -> Records.listAll")
    Ok(Json.toJson(records)) as JSON
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
  
  def list(page: Int, orderBy: Int, filter: String = "") = withAuth { username => implicit request =>
    val records = models.Records.findPage(page, orderBy, filter)
    Logger.debug(s"list :: listing ${records.total} for page=$page, orderBy=$orderBy, filter=$filter")
    Ok(views.html.records.list(records.items, 0, page, orderBy, records.total.toInt))
  }
  
  def show(id: Int) = withAuth { username => implicit request =>
    models.Records.findById(id) match {
      case Success(r) => {
        Logger.debug(s"show :: found for id=$id")
        Ok(views.html.records.show(r.getOrElse(null)))
      }
      case Failure(e) => { Logger.debug(s"show :: id=$id NotFound"); NotFound }
    }
  }
  
  def edit(id: Int) = withAuth { username => implicit request =>
    models.Records.findById(id) match {
      case Success(r) => {
        Logger.debug(s"edit :: found id=$id, editing for $r")
        val form = recordForm.fill(r.getOrElse(null))
        Ok(views.html.records.edit(r.getOrElse(null), form))
      }
      case Failure(e) => { Logger.debug(s"edit :: id=$id NotFound"); NotFound }
    }
  }
  
  def getRecordCount(id: Int) : Int = {
    models.Records.findByAccountId(id) match {
      case Success(r) => { Logger.debug(s"getRecordCount :: returning ${r.length} for id=$id"); r.length }
      case Failure(r) => { Logger.debug(s"getRecordCount :: returning 0 for id=$id"); 0 }
    }
  }
  
  def listAllAccountIds(page: Int, orderBy: Int, filter: String = "") = withAuth { username => implicit request =>
  	val accounts = models.Records.listAccountIdsWithCount(page, orderBy, filter)
  	Logger.debug(s"listAllAccountIds :: listing id's for ${accounts.size.toInt} accounts, page $page, orderBy $orderBy, filter $filter")
  	Ok (views.html.accounts.list(accounts, page, orderBy, accounts.size.toInt))
  }
  
  def listByDomainId(id: Int, page: Int, orderBy: Int) = withAuth { username => implicit request =>
    models.Records.findByDomainId(id) match {
      case Success(r) => {
        Logger.debug(s"listByDomainId :: listing for id $id, page $page, orderBy $orderBy, with success ${r.count(r => true)}")
        Ok(views.html.records.list(r, id, page, orderBy, r.length))
      }
      case Failure(r) => NotFound
    }
  }
  
  def newRecord(domainId: Int) = Action { implicit request =>
    Logger.debug(s"newRecord :: for $domainId")
    Ok(views.html.records.create(recordForm, domainId))
  }
}