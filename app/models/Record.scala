package models

import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.language.reflectiveCalls
import scala.util.Try
import utils.DateTimeUtils._

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

object RecordType extends Enumeration {
  type Type = Value
  val A,
  CNAME,
  NAPTR,
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
  def autoInc = * returning id

  val byId = createFinderBy(_.id)
  val byDomainId = createFinderBy(_.domainId)
  val byName = createFinderBy(_.name)
  val byContent = createFinderBy(_.content)
  val byAccountId = createFinderBy(_.accountId)
    
  def findAll = DB.withSession { 
    implicit session =>
      (for (r <- Records.sortBy(_.id) ) yield r).list 
  }
    
  def findById(id: Int) = DB.withSession {
    implicit session =>
      Try(Records.byId(id).firstOption) 
  }
    
  def findByAccountId(accountId: Int) = DB.withSession { 
    implicit session => 
      Try(Records.byAccountId(accountId).list) 
  }
    
  def findByDomainId(domainId: Int) = DB.withSession {
    implicit session =>
      Try(Records.byDomainId(domainId).list) 
  }
    
  def listAccountIds = DB.withSession { 
    implicit session => 
      (for (r <- Records) yield r.accountId).list.distinct  
  }
    
  def delete(id: Int) = DB.withSession { 
    implicit session => 
      Try(Records.where(_.id === id).delete) 
  }
    
  def insert(record: Record) = DB.withSession { 
    implicit session => 
      Try(Records.autoInc.insert((record)))
  }
    
  def update(id: Int, record: Record) = DB.withSession { 
    implicit session => {
      val recordToUpdate = record.copy(Some(id), record.domainId, record.name, record.recordType, record.content, record.ttl, record.priority, nowInUnixTime, record.accountId)
      Logger.debug("update: updating recordid=" + id + ", record=" + record + ", previous=" + Records.findById(id))
      Try(Records.where(_.id === id).update(recordToUpdate))
    }
  }

  def findPage(page: Int = 0, orderField: Int): Page[Record] = {
    val offset = pageSize * page
    
    DB.withSession {
      implicit session =>
        val records = (
          for {r <- Records.sortBy(r => orderField match {
            case 1 => r.id.asc
            case -1 => r.id.desc
            case 2 => r.name.asc
            case -2 => r.name.desc
          })
            .drop(offset)
            .take(pageSize)
          } yield r).list
          
          val totalRows = (for (r <- Records) yield r.id).list.size
          Page(records, page, offset, totalRows)
    }
  }
  
  implicit val recordFormat = Json.format[Record]
}