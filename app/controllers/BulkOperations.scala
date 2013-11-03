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
import play.api.mvc.Controller

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
        val returns: Boolean = runBulkCreateTask(bulkCreate)
        Ok("Bulk Job Successful")
      }
    )
  }
  
  def runBulkCreateTask(task: BulkCreateOperation): Boolean = {
    Logger.debug(s"bulkCreateRecordsForAccounts: running with task $task")
    BulkOperation.performBulkCreateOperation(task)
    true
  }

}