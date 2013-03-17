package models

import scala.util.Try

import play.Logger
import play.api.Play.current
import play.api.db.slick.Config.driver.simple.Session
import play.api.db.slick.Config.driver.simple.columnBaseToInsertInvoker
import play.api.db.slick.Config.driver.simple.columnExtensionMethods
import play.api.db.slick.Config.driver.simple.productQueryToUpdateInvoker
import play.api.db.slick.Config.driver.simple.queryToDeleteInvoker
import play.api.db.slick.Config.driver.simple.queryToQueryInvoker
import play.api.db.slick.Config.driver.simple.Table
import play.api.db.slick.Config.driver.simple.tableToQuery
import play.api.db.slick.Config.driver.simple.valueToConstColumn
import play.api.db.slick.DB
import play.api.db.slick.Profile

case class Domain (
    id: Option[Int] = None,
    name: String,
    master: String,
    lastCheck: Long,
    domainType: String,
    notifiedSerial: Long,
    account: String
)

case class DomainId(id: Int)
case class DomainForCreate(name: String, master: String, domainType: String, notifiedSerial: Long, account: String)
case class DomainForUpdate(id: Int, master: String, lastCheck: Long, domainType: String, account: String)
case class DomainForRead(id: Int, name: String, master: String, lastCheck: Long, domainType: String, notifiedSerial: Long, account: String)

object DomainType extends Enumeration {
  type Type = Value
  val MASTER, SLAVE, NATIVE = Value
}

object Domain {
  import play.api.Play.current
  
  object DomainTable extends Table[Domain]("domains") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey, O.NotNull)
    def name = column[String]("name", O.NotNull)
    def master = column[String]("master", O.Default(null))
    def lastCheck = column[Long]("last_check")
    def domainType = column[String]("type", O.NotNull)
    def notifiedSerial = column[Long]("notified_serial")
    def account = column[String]("account", O.Default(null))
    def * = id.? ~ name ~ master ~ lastCheck ~ domainType ~ notifiedSerial ~ account <> (Domain.apply _, Domain.unapply _)
    def autoInc = * returning id
    
    def findAll(implicit session: Session) = (for (d <- this) yield d).list
    def findById(id: Int)(implicit session: Session) = createFinderBy(_.id).first(id)
    def delete(id: Int)(implicit session: Session) = this.where(_.id === id).mutate(_.delete)
    def deleteAll(implicit session: Session) = (for (d <- this) yield d).delete
    def forInsert = name ~ master ~ lastCheck ~ domainType ~ notifiedSerial ~ account <>
      ({ (name, master, lastCheck, domainType, notifiedSerial, account) => Domain(None, name, master, lastCheck, domainType, notifiedSerial, account)},
          {d: Domain => Some((d.name, d.master, d.lastCheck, d.domainType, d.notifiedSerial, d.account)) }) returning id
  }

  def findById (id: Int): Try[Domain] = DB.withSession { implicit session =>
    Logger.debug(s"findById :: finding by Id=$id")
    Try(DomainTable.findById(id))
  }
  
  def findAll: List[Domain] = {
    Logger.debug("find: finding " )
    val domains = DB.withSession { implicit session =>
      DomainTable.findAll
    }
    domains
  }
  
  def create(domain: DomainForCreate): Try[DomainId] = DB.withSession { implicit session =>
    Logger.debug("create for domain " + domain + " at time " + nowInUnixTime)
    Try(
      DomainId(
        DomainTable.forInsert.insert(
          Domain(None,
            domain.name,
            domain.master,
            nowInUnixTime,
            domain.domainType,
            domain.notifiedSerial,
            domain.account
          ))))
  }
    
  def update(domain: DomainForUpdate): Try[Unit] = DB.withSession { implicit session =>
    Logger.debug("update :: updating for domain " + domain + " with id=" + domain.id)
    Try((DomainTable.filter(d => d.id === domain.id).map(d => d.master ~ d.lastCheck ~ d.domainType ~ d.account))
        .update(domain.master, nowInUnixTime, domain.domainType, domain.account))
  }
  
  def delete(id: Int) = {
    val deleted = DB.withSession { implicit session =>
      DomainTable.delete(id)
    }
    deleted
  }
  
  def deleteAll = {
    val deleted = DB.withSession { implicit session =>
      DomainTable.deleteAll
    }
    deleted
  }
  
  def nowInUnixTime = { (System.currentTimeMillis / 1000L).toInt }  
}