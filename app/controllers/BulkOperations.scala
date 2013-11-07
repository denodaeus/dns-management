package controllers

import scala.util.Failure
import scala.util.Success

import models.Record
import models.BasicRecord
import models.BulkOperation
import models.BulkCreateOperation
import models.BasicSRVRecord
import models.SrvContent
import models.BasicHostNameARecord

import play.Logger
import play.api.data.Form
import play.api.data.Forms.seq
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.mvc.Action
import play.api.mvc.WebSocket
import play.api.mvc.Controller
import play.api.libs.json._
import play.api.libs.iteratee._


object BulkOperations extends Controller with Secured {
  
  val bulkCreateForm: Form[BulkCreateOperation] = Form(
    mapping(
      "accounts" -> nonEmptyText,
      "srv" -> mapping(
        "domainId" -> number,
        "subdomain" -> nonEmptyText,
        "proto" -> nonEmptyText,
        "service" -> nonEmptyText,
        "ttl" -> number,
        "content" -> seq(
          mapping(
            "weight" -> number,
            "port" -> number,
            "serverId" -> number
          )(SrvContent.apply)(SrvContent.unapply)
        )
      )(BasicSRVRecord.apply)(BasicSRVRecord.unapply)
    )(BulkCreateOperation.apply)(BulkCreateOperation.unapply)
  )
  
  def index = withAuth { username => implicit request =>
    Ok
  }
  
  def migrate() = withAuth { username => implicit request =>
    Ok(views.html.accounts.bulkmigrate())
  }
  
  def bulkMigrate = withAuth { username => implicit request =>
    Ok
  }
  
  def createSRVRecordsForAccount(accountId: Int) = {
    Logger.debug("createSRVRecordsForAccount :: generating SRV record " + s" for account ${accountId}")   
  }
  
  def bulkMigrateAccountsOffCluster(accounts: Seq[Int]) {
  }
  
  def bulkCreate = withAuth { username =>
    implicit request =>
    Ok(views.html.accounts.bulkcreate(bulkCreateForm))
  }
  
  def bulkCreateRecordsForAccounts = Action { implicit request =>
    bulkCreateForm.bindFromRequest.fold(
      formWithErrors => {
        //BadRequest(views.html.accounts.bulkcreate(formWithErrors))
        Logger.debug("bulkCreateRecordsForAccounts :: BadRequest, errors=" +formWithErrors.errorsAsJson + ", request=" + request.body + ", form=" + formWithErrors.toString())
        BadRequest("Please correct the following Errors: " + formWithErrors.errorsAsJson)
      },
      bulkCreate => {
        val (id, results ) = runBulkCreateTask(bulkCreate)
        Ok(views.html.accounts.bulkstatus(id, results)).flashing("success" -> s"Task Started for id=$id")
      }
    )
  }
  
  def runBulkCreateTask(task: BulkCreateOperation) = {
    import play.api.libs.concurrent.Execution.Implicits._
    Logger.debug(s"bulkCreateRecordsForAccounts: running with task $task")
    val id = System.currentTimeMillis / 1000L
    val results = BulkOperation.performBulkCreateOperation(task, id)
    (id, results)
  }
  
  def watchOperationStatus(jobId: Long) = WebSocket.using[JsValue] { implicit request =>
    import play.api.libs.concurrent.Execution.Implicits._
    val(out, channel) = (BulkOperation.out, BulkOperation.channel)
    Logger.debug(s"watchOperationStatus :: for job=$jobId, out=$out, channel=$channel")
    val in = Iteratee.foreach[JsValue] {
      msg => Logger.debug(s" pushing message to channel :: $msg")
      channel.push(msg)
    }
    (in, out)
  }
  
}