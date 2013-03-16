package models

import models.Domain.DomainTable
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB


case class Record (
  id: Int,
  domainId: Int,
  name: String,
  recordType: String,
  content: String,
  ttl: Int,
  priority: Int,
  modified: Int
)

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

object Record extends RecordDAO {
  import play.api.Play.current
  
  def findById (id: Int): Option[Record] = {
    val record = DB.withSession { implicit session =>
      RecordTable.findById(id)
    }
    record
  }
  
  def find(): List[Record] = {
    val records = DB.withSession { implicit session =>
      RecordTable.findAll
    }
    records
  }
}

trait RecordDAO {
  object RecordTable extends Table[Record]("records") {
    def id = column[Int]("id", O.NotNull, O.AutoInc, O.PrimaryKey)
    def domainId = column[Int]("domain_id", O.NotNull)
    def name = column[String]("name", O.Default(null))
    def recordType = column[String]("type", O.Default(null))
    def content = column[String]("content", O.Default(null))
    def ttl = column[Int]("ttl")
    def priority = column[Int]("prio")
    def modified = column[Int]("change_date")
    def domainFK = foreignKey("domain_exists", domainId, DomainTable)(_.id)
    def * = id ~ domainId ~ name ~ recordType ~ content ~ ttl ~ priority ~ modified <> (Record.apply _, Record.unapply _)
    
    // Queries
    def findAll(implicit session: Session) = (for (r <- RecordTable) yield r).list
    def findById(id: Int)(implicit session: Session) = createFinderBy(_.id).firstOption(id)

  }
}