package models

import models.Domain.DomainTable
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.language.reflectiveCalls
import scala.util.Try

case class Record (
  id: Option[Int] = None,
  domainId: Int,
  name: String,
  recordType: String,
  content: String,
  ttl: Int,
  priority: Int,
  changeDate: Int
)

case class RecordId(id: Int)
case class RecordForCreate(domainId: Int, name: String, recordType: String, content: String, ttl: Int, priority: Int)
case class RecordForUpdate(id: Int, name: String, recordType: String, content: String, ttl: Int, priority: Int)
case class RecordForRead(id: Int, domainId: Int, name: String, recordType: String, content: String, ttl: Int, priority: Int, changeDate: Int)
case class RecordsAsList(records: List[Record])

object RecordType extends Enumeration {
  type Type = Value
  val A,
      AAAA,
      CNAME,
      MX,
      NAPTR,
      NS,
      PTR,
      SOA,
      SRV,
      TXT,
      URL = Value
}

object Record {
  import play.api.Play.current
  
  val RecordTable = new Table[Record]("records") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def domainId = column[Int]("domain_id", O.NotNull)
    def name = column[String]("name", O.Default(null))
    def recordType = column[String]("type", O.Default(null))
    def content = column[String]("content", O.Default(null))
    def ttl = column[Int]("ttl")
    def priority = column[Int]("prio")
    def changeDate = column[Int]("change_date")
    def domainFK = foreignKey("domain_exists", domainId, DomainTable)(_.id)
    def * = id.? ~ domainId ~ name ~ recordType ~ content ~ ttl ~ priority ~ changeDate <> (Record.apply _, Record.unapply _)    
    def autoInc = * returning id
    
    // Queries
    def findAll(implicit session: Session) = (for (r <- this) yield r).list
    def findById(id: Int)(implicit session: Session) = createFinderBy(_.id).first(id)
    def delete(id: Int)(implicit session: Session) = this.where(_.id === id).mutate(_.delete)
    def deleteAll(implicit session: Session) = (for (r <- this) yield r).delete
    def forInsert = domainId ~ name ~ recordType ~ content ~ ttl ~ priority ~ changeDate <>
      ({ (domainId, name, recordType, content, ttl, priority, changeDate) => Record(None, domainId, name, recordType, content, ttl, priority, changeDate )},
        {r: Record => Some((r.domainId, r.name, r.recordType, r.content, r.ttl, r.priority, r.changeDate)) }) returning id
  }
  
  def findById (id: Int): Try[Record] = DB.withSession { implicit session =>
    Logger.debug(s"findById :: finding by Id=$id")
    Try(RecordTable.findById(id))
  }
  
  def findAll: List[Record] = {
    Logger.debug("find: finding " )
    val records = DB.withSession { implicit session =>
      RecordTable.findAll
    }
    records
  }
  
  def create(record: RecordForCreate): Try[RecordId] = DB.withSession { implicit session =>
    Logger.debug("create for record " + record + " at time " + nowInUnixTime)
    Try(
      RecordId(
        RecordTable.forInsert.insert(
          Record(None,
            record.domainId,
            record.name,
            record.recordType,
            record.content,
            record.ttl,
            record.priority,
            nowInUnixTime
          ))))
  }
    
  def update(record: RecordForUpdate): Try[Unit] = DB.withSession { implicit session =>
    Logger.debug("update :: updating for record " + record + " with id=" + record.id)
    Try((RecordTable.filter(r => r.id === record.id).map(r => r.name ~ r.recordType ~ r.content ~ r.ttl ~ r.priority ~ r.changeDate))
        .update(record.name, record.recordType, record.content, record.ttl, record.priority, nowInUnixTime))
  }
  
  def delete(id: Int) = {
    val deleted = DB.withSession { implicit session =>
      RecordTable.delete(id)
    }
    deleted
  }
  
  def deleteAll = {
    val deleted = DB.withSession { implicit session =>
      RecordTable.deleteAll
    }
    deleted
  }
  
  def nowInUnixTime = { (System.currentTimeMillis / 1000L).toInt }
}

