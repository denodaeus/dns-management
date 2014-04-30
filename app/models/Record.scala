package models

import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.language.reflectiveCalls
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import utils.DateTimeUtils._
import collection.immutable.SortedMap

case class Record (
  id: Option[Int],
  domainId: Int,
  name: String,
  recordType: String,
  content: String,
  ttl: Int,
  priority: Int,
  changeDate: Int,
  accountId: Int
)

case class BaseRecord(name: String, domainId: Int, recordType: String, ttl: Int, priority: Int, changeDate: Int)
case class BasicRecord(domainId: Int, name: String, content: String, recordType: String, ttl: Int, priority: Int)
case class BasicSRVRecord(domainId: Int, subdomain: String, proto: String, service: String, ttl: Int, content: Seq[SrvContent])
case class BasicARecord(record: BasicRecord, content: AContent)
case class AContent(content: IPAddress)
case class SrvContent(priority: Int, weight: Int, port: Int, serverId: Int)

case class IPAddress(address: String)

object BasicSRVRecord {
  def formContent(weight: Int, port: Int, hostName: String):String = {
    s"$weight $port $hostName"
  }
}

object IPAddress {
  
  def apply(a: String, b: String, c:String, d:String):String = s"$a.$b.$c.$d"
  
  def unapply(ip: String): Option[(String, String, String, String)] = {
    val tokens = ip split "\\."
    if (tokens.length == 4 && isValid(tokens)) Some(tokens(0), tokens(1), tokens(2), tokens(3)) else None
  }
  
  private def isValid(tokens: Array[String]):Boolean = {
    tokens forall { elem =>
      try {
        val intValue = elem.toInt
        intValue >= 0 && intValue <= 255
      } catch {
        case _: Throwable => false
      }
    }
  }
}

object RecordType extends Enumeration {
  type Type = Value
  val A,
  CNAME,
  NAPTR,
  NS,
  SOA,
  SRV = Value
}

object Records extends Table[Record]("records"){
  import play.api.Play.current
  
  lazy val pageSize = 20

  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def domainId = column[Int]("domain_id", O.NotNull)
  def name = column[String]("name", O.Default(null))
  def recordType = column[String]("type", O.Default(null))
  def content = column[String]("content", O.Default(null))
  def ttl = column[Int]("ttl")
  def priority = column[Int]("prio")
  def changeDate = column[Int]("change_date")
  def accountId = column[Int]("account_id")
  def domainFK = foreignKey("domain_exists", domainId, Domains)(_.id)
  def * = id.? ~ domainId ~ name ~ recordType ~ content ~ ttl ~ priority ~ changeDate ~ accountId <> (Record, Record.unapply _)    
  def autoInc = domainId ~ name ~ recordType ~ content ~ ttl ~ priority ~ changeDate ~ accountId returning id

  val byId = createFinderBy(_.id)
  val byDomainId = createFinderBy(_.domainId)
  val byName = createFinderBy(_.name)
  val byContent = createFinderBy(_.content)
  val byAccountId = createFinderBy(_.accountId)
    
  def findAll = DB.withSession { 
    implicit session: Session =>
      (for (r <- Records.sortBy(_.id) ) yield r).list 
  }
    
  def findById(id: Int) = DB.withSession {
    implicit session: Session =>
      Try(Records.byId(id).firstOption) 
  }
    
  def findByAccountId(accountId: Int) = DB.withSession { 
    implicit session: Session => 
      Try(Records.byAccountId(accountId).list) 
  }
    
  def findByDomainId(domainId: Int, page: Int, offset: Int) = DB.withSession {
    val offset = pageSize * page
    implicit session: Session =>
      Try(
            (for (r <- Records.where(_.domainId === domainId)) yield r)
          	.sortBy(_.id)
          	.drop(offset)
          	.take(pageSize)
          	.list
      )
  }
  
  def findByContent(content: String) = DB.withSession {
    implicit session: Session =>
      (for (r <- Records.where(_.content === content)) yield r).list.toSeq
  }
    
  def listAccountIds(filter: String = "") = DB.withSession { 
    implicit session: Session => 
      (for (r <- Records) yield r.accountId)
      	.list
      	.distinct
      	.sorted  
  }
  
  def listAccountIdsWithCount: Map[Int, Int] = DB.withSession {
    implicit session: Session => {
      val records = Records.groupBy(_.accountId).map{ case(id, c) => id -> id.count }
      SortedMap(records.toMap.toSeq:_*)
    }
  }
  
  def listAccountIdsWithCount(page: Int, offset: Int, filter: String = ""): Map[Int, Int] = DB.withSession {
    val offset = pageSize * page
    implicit session: Session => {
      if (filter != "") {
        val records = (Records.findByAccountId(filter.toInt)).map{ case(r) => filter.toInt -> r.size}
        records match {
          case Success(r) => Map(r);
          case Failure(r) => Map(0 -> 0)
        }
      }
      else {
    	  val records = (Records.groupBy(_.accountId)
          .map{ case(id, c) => id -> id.count })
          .drop(offset)
          .take(pageSize)
          SortedMap(records.toMap.toSeq:_*)
      }
    }
  }
  
  def listRecordCountForHost(host: String, recordType: String): Int = DB.withSession {
    implicit sesion: Session => {
      Records.where(r => r.content.like(s"$host") && r.recordType === recordType).length.run
    }
  }

  def delete(id: Int) = DB.withSession { 
    implicit session: Session => 
      Try(Records.where(_.id === id).delete) 
  }
    
  def insert(r: Record) = DB.withSession { 
    implicit session: Session => 
      Try(Records.autoInc.insert(r.domainId, r.name , r.recordType, r.content, r.ttl, r.priority, nowInUnixTime, r.accountId))
  }
    
  def update(id: Int, record: Record) = DB.withSession { 
    implicit session: Session => {
      val recordToUpdate = record.copy(Some(id), record.domainId, record.name, record.recordType, record.content, record.ttl, record.priority, nowInUnixTime, record.accountId)
      Logger.debug("update: updating recordid=" + id + ", record=" + record + ", previous=" + Records.findById(id))
      Try(Records.where(_.id === id).update(recordToUpdate))
    }
  }
  
  def findFilteredPage(records: List[Record], page: Int = 0, orderField: Int): Page[Record] = {
    val offset = pageSize * page
    val results = (records.sortBy(r => r.id)).drop(offset).take(pageSize)
    Page(results, page, offset, records.size, pageSize)
  }

  def findPage(page: Int = 0, orderField: Int, filter: String = ""): Page[Record] = {
    val offset = pageSize * page
    
    DB.withSession {
      implicit session: Session =>
        val records = (
          for {r <- Records.sortBy(r => orderField match {
            case 1 => r.id.asc
            case -1 => r.id.desc
            case 2 => r.name.asc
            case -2 => r.name.desc
          })
            .filter(r => 
              (r.name.like(s"%${filter}%") 
           || r.content.like(s"%${filter}%")
           || r.recordType.like(s"%${filter}%")))
            .drop(offset)
            .take(pageSize)
          } yield r).list
          
          val totalRows = (for (r <- Records) yield r.id).list.size
          Page(records, page, offset, totalRows, pageSize)
    }
  }
  
  implicit val recordFormat = Json.format[Record]
}