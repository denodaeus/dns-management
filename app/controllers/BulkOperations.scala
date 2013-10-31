package controllers

import scala.util.Failure
import scala.util.Success

import models.Record
import models.BasicRecord
import models.BulkOperation
import models.BulkCreateOperation

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
      "records" -> seq(
         mapping(
           "domainId" -> number,
           "name" -> nonEmptyText,
           "recordType" -> nonEmptyText,
           "content" -> nonEmptyText,
           "ttl" -> number,
           "priority" -> number,
           "accountId" -> number
         )(BasicRecord.apply _)(BasicRecord.unapply _)
      )
    )(BulkCreateOperation.apply)(BulkCreateOperation.unapply _)
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

}