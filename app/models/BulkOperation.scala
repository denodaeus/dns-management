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
case class BulkCreateOperation(accounts: String, srv: BasicSRVRecord)
case class BasicHostNameARecord(serverId: Int, content: String, priority: Int )

object BulkOperation {
  
  lazy val domains: Map[Int, String] = models.Domains.findAll.map(d => (d.id.get -> d.name)).toMap
  
  def parseAccountsToSeq(accounts: String, delimiter: Char): Seq[Int] = {
    val list = accounts.split(delimiter).map(_.toInt)
    Logger.debug(s"parseAccountsToSeq :: parsed accounts ${list.toString()}")
    list
  }
  
  def getServerFromServerId(id: Int): Server = {
    models.Servers.findById(id).get
  }
  
  def performBulkCreateOperation(task: BulkCreateOperation) = {
    val acctList = parseAccountsToSeq(task.accounts, ',')
    for(account <- acctList) {
      for(content <- task.srv.content.seq) {
        Logger.debug(s"performBulkCreateOperation :: for account $account, operating on record ${content.toString}")
        val server = getServerFromServerId(content.serverId)
        val srvRecord = createSrvRecordForAccountIfItDoesntExist(account, content.weight, content.port, task.srv, server)
        insertOrUpdateRecord(srvRecord)
      }
    }
  }
  
  def createARecordForAccountIfItDoesntExist(accountId: Int, record: BasicRecord): Record = {
    val name = replaceTokenWithAccountId(accountId, domainFragment = record.name) + "." + domains.get(record.domainId).get
    val r = Record(None, record.domainId, name, "A", record.content, record.ttl, record.priority, 1, accountId)
    Logger.debug(s"createARecordForAccountIfItDoesntExist :: for account $accountId, created record ${r.toString()}, from parameters ${record.toString}")
    r
  }
  
  def createSrvRecordForAccountIfItDoesntExist(accountId: Int, weight: Int, port: Int, srv: BasicSRVRecord, server: Server) = {
    val defaultPriority = 0
    val name = replaceTokenWithAccountId(accountId, domainFragment = srv.subdomain) + "." + domains.get(srv.domainId).get
    val srvContent = BasicSRVRecord.formContent(weight, port, server.hostName)
    val srvName = formSrvDomain(srv.service, srv.proto, name)
    val r = Record(None, srv.domainId, srvName, "SRV", srvContent, srv.ttl, defaultPriority, 1, accountId)
    Logger.debug(s"createSrvRecordForAccountIfItDoesntExist :: created record ${r.toString()}, from content -> account=$accountId, srv=$srv, record=$srv")
    r
  }
  
  def formSrvDomain(service: String, proto: String, domain: String) = {
    s"_${service}._${proto}.${domain}"
  }
  
  def replaceTokenWithAccountId(accountId: Int, token: String = "\\$\\{accountId\\}", domainFragment: String) = {
    domainFragment.replaceAll(token, s"${accountId}")
  }
  
  def insertOrUpdateRecords(records: Seq[Record]) = {
    for(record <- records) {
      models.Records.insert(record) match {
        case Success(r) => {
          Logger.debug(s"insertOrUpdateRecords :: inserted ${record}, returns ${r}")
        }
        case Failure(e) => {
          Logger.debug(s"insertOrUpdateRecords :: failed to update ${record}, reason=${e.getMessage()}")
        }
      }
    }
  }
  
  def insertOrUpdateRecord(record: Record) = {
    models.Records.insert(record) match {
      case Success(r) => Logger.debug(s"insertOrUpdateRecord :: inserted $record")
      case Failure(e) => Logger.debug(s"insertOrUpdateRecord :: failure inserting for $record, failure $e")
    }
  }
  
}