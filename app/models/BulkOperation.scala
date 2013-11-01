package models

import play.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.collection.immutable.StringOps._

case class BulkAccountOperation(operation: String, accounts: Seq[Int])
case class BulkOperation(operation: String, accounts: String)
case class BulkCreateOperation(accounts: String, records: Seq[BasicRecord])

object BulkOperation {
  
  lazy val domains: Map[Int, String] = models.Domains.findAll.map(d => (d.id.get -> d.name)).toMap
  
  def bulkCreateRecordsForAccount(accountId: Int, task: String = "SRV") = {
    
  }
  
  def parseAccountsToSeq(accounts: String, delimiter: Char): Seq[Int] = {
    val list = accounts.split(delimiter).map(_.toInt)
    Logger.debug(s"parseAccountsToSeq :: parsed accounts ${list.toString()}")
    list
  }
  
  def performBulkCreateOperation(task: BulkCreateOperation) = {
      val acctList = parseAccountsToSeq(task.accounts, ',')
      for(account <- acctList) {
        for(record <- task.records.seq) {
          Logger.debug(s"performBulkCreateOperation :: for account $account, operating on record ${record.toString}")
          val aRecord = createARecordForAccountIfItDoesntExist(account, record)
        }
      }
  }
  
  def createARecordForAccountIfItDoesntExist(accountId: Int, record: BasicRecord): Record = {
    val name = replaceTokenWithAccountId(accountId, domainFragment = record.name) + "." + domains.get(record.domainId).get
    val r = Record(None, record.domainId, name, record.recordType, record.content, record.ttl, record.priority, 1, accountId)
    Logger.debug(s"createARecordForAccountIfItDoesntExist :: for account $accountId, created record ${r.toString()}, from parameters ${record.toString}")
    r
  }
  
  def createSrvRecordForAccountIfItDoesntExist() = {
    
  }
  
  def replaceTokenWithAccountId(accountId: Int, token: String = "\\$\\{accountId\\}", domainFragment: String) = {
    domainFragment.replaceAll(token, s"${accountId}")
  }
  
}