package controllers

import scala.util.Failure
import scala.util.Success

import models.Record
import models.BasicRecord
import models.BulkOperation
import models.BulkCreateOperation
import models.BasicSRVRecord
import models.SrvContent

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
        "proto" -> nonEmptyText,
        "service" -> nonEmptyText,
        "content" -> seq(
            mapping(
              "weight" -> number,
              "port" -> number,
              "A" -> mapping(
                "domainId" -> number,
                "name" -> nonEmptyText,
                "recordType" -> nonEmptyText,
                "content" -> nonEmptyText,
                "ttl" -> number,
                "priority" -> number
              )(BasicRecord.apply)(BasicRecord.unapply)
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
      formWithErrors => BadRequest(views.html.accounts.bulkcreate(formWithErrors)),
      bulkCreate => {
        val operation = bulkCreate
        val records = bulkCreate.records.seq
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