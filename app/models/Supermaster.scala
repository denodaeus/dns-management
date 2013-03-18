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

case class Supermaster (
  id: Option[Int] = None,
  ip: String, 
  nameServer: String, 
  account: String
)

case class SupermasterId (id: Int)
case class SupermasterForCreate (ip: String, nameServer: String, account: String)
case class SupermasterForUpdate (id: Int, ip: String, nameServer: String, account: String)
case class SupermasterForRead(id: Int, ip: String, nameServer: String, account: String)

object Supermaster {
  import play.api.Play.current
  
  object SupermasterTable extends Table[Supermaster]("supermasters") {
    def id = column[Int]("id", O.NotNull, O.PrimaryKey, O.AutoInc)
    def ip = column[String]("ip", O.NotNull)
    def nameServer = column[String]("nameserver", O.NotNull)
    def account = column[String]("account")
    def autoInc = * returning id
    
    def * = id.? ~ ip ~ nameServer ~ account <> (Supermaster.apply _, Supermaster.unapply _)
    
    def findAll(implicit session: Session) = (for (s <- this) yield s).list
    def findById(id: Int)(implicit session: Session) = createFinderBy(_.id).first(id)
    def delete(id: Int)(implicit session: Session) = this.where(_.id === id).mutate(_.delete)
    def deleteAll(implicit session: Session) = (for (s <- this) yield s).delete
    def forInsert = ip ~ nameServer ~ account <>
      ({ (ip, nameServer, account) => Supermaster(None, ip, nameServer, account)},
          {s: Supermaster => Some((s.ip, s.nameServer, s.account)) }) returning id
  }
  
  def findById (id: Int): Try[Supermaster] = DB.withSession { implicit session =>
    Logger.debug(s"findById :: finding by Ip=$id")
    Try(SupermasterTable.findById(id))
  }
  
  def findAll: List[Supermaster] = {
    Logger.debug("find: finding " )
    val supermasters = DB.withSession { implicit session =>
      SupermasterTable.findAll
    }
    supermasters
  }
  
  def create(supermaster: SupermasterForCreate): Try[SupermasterId] = DB.withSession { implicit session =>
    Logger.debug("create for supermaster " + supermaster + " at time ")
    Try(
      SupermasterId(
        SupermasterTable.forInsert.insert(
          Supermaster(None, supermaster.ip, supermaster.nameServer, supermaster.account
          ))))
  }
  
    
  def update(supermaster: SupermasterForUpdate): Try[Unit] = DB.withSession { implicit session =>
    Logger.debug("update :: updating for domain " + supermaster + " with id=" + supermaster.ip)
    Try((SupermasterTable.filter(s => s.ip === supermaster.ip).map(s => s.ip ~ s.nameServer ~ s.account))
        .update(supermaster.ip, supermaster.nameServer, supermaster.account))
  }
  
  def delete(id: Int) = {
    val deleted = DB.withSession { implicit session =>
      SupermasterTable.delete(id)
    }
    deleted
  }
  
  def deleteAll = {
    val deleted = DB.withSession { implicit session =>
      SupermasterTable.deleteAll
    }
    deleted
  }

}