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
    ) (Record.apply)(Record.unapply).verifying("Failed form validation: " +
        " Prefix contains domain value.", fields => fields match {
      case record => {
        val domainName = models.Domains.getNameForId(record.domainId);
        record.name.endsWith(domainName) &&
        !(record.name.stripSuffix(domainName).contains(domainName))
      }
    })
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
  
  def deleteOne(id: Int) = withAuth { username => implicit request =>
    models.Records.delete(id) match {
      case Success(r) => Redirect(routes.Records.list(0,1,"")).flashing("success" -> s"Record $id deleted successfully.")
      case Failure(e) => Redirect(routes.Records.show(id)).flashing("error" -> s"Failed to delete record $id, reason=${e.getMessage()}")
    }
  }
  
  def delete() = withAuth { username => implicit request =>
    request.body.asJson.map { json =>
      json.validate[Seq[Int]].fold(
        invalid => BadRequest(s"delete :: error deleting, malformed json=$json"),
        valid => {
          for (record <- valid) {
            Logger.debug(s"delete :: deleting record $record")
            models.Records.delete(record) match {
              case Success(r) => Logger.debug("successfully deleted " + r)
              case Failure(e) => Logger.debug("failed to delete, reason=" + e)
            }
          }
          Ok(s"Successfully deleted json=$json")
        }
      )
    }.getOrElse(BadRequest)
  }

  // VIEWS SECTION FOR TEMPORARY VIEWS
  
  def update() = withAuth { username => implicit request =>
  	recordForm.bindFromRequest.fold(
  	  formWithErrors => BadRequest(views.html.records.edit(formWithErrors)),
  	  record => {
  	    models.Records.update(record.id.get, record) match {
  	      case Success(r) => Redirect(s"/records/show/${record.id.get}").flashing("success" -> "Successful Edit")
  	      case Failure(e) => {
  	        Logger.debug(s"update :: failed to update record with id=${record.id}, reason=${e.printStackTrace()}")
  	        Redirect(routes.Records.show(record.id.get)).flashing("error" -> s"Error updating record ${record.id}; ${e.getMessage()}")
  	      }
  	    }
  	  }
  	)
  }
  
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
        Ok(views.html.records.edit(form))
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
    models.Records.findByDomainId(id, page, orderBy) match {
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
  
  def createRecordNoDomainId() = withAuth { username => implicit request =>
  	Ok(views.html.records.create(recordForm, 0))
  }
  
  def createRecord() = withAuth { username => implicit request =>
  	recordForm.bindFromRequest.fold(
  	  formWithErrors => BadRequest(views.html.records.create(formWithErrors, 0)).flashing("error" -> s"Errors on submission: ${formWithErrors.errorsAsJson}"),
  	  record => {
  	    models.Records.insert(record) match {
  	      case Success(r) => Redirect(routes.Records.show(r)).flashing("success" -> s"Successfully inserted record $r")
  	      case Failure(r) => BadRequest(views.html.records.create(recordForm, 0)).flashing("error" -> s"error inserting record $r, error=> ${r.printStackTrace()}")
  	    }
  	  }
  	)
  }
}