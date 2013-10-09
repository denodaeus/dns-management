package models

import scala.util.Try

import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.language.reflectiveCalls
import scala.util.Try
import utils.DateTimeUtils._

case class Domain (
    id: Option[Int],
    name: String,
    master: Option[String],
    lastCheck: Option[Long],
    domainType: String,
    notifiedSerial: Long,
    account: Option[String]
)

object DomainType extends Enumeration {
  type Type = Value
  val MASTER, SLAVE, NATIVE = Value
}

object Domains extends Table[Domain]("domains") {
  import play.api.Play.current
  
  lazy val pageSize = 20

  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name", O.NotNull)
  def master = column[String]("master", O.Default(null))
  def lastCheck = column[Long]("last_check")
  def domainType = column[String]("type", O.NotNull)
  def notifiedSerial = column[Long]("notified_serial")
  def account = column[String]("account", O.Default(null))
  def * = id.? ~ name ~ master.? ~ lastCheck.? ~ domainType ~ notifiedSerial ~ account.? <> (Domain, Domain.unapply _)
  def autoInc = * returning id
    
  val byId = createFinderBy(_.id)
  val byName = createFinderBy(_.name)
    
  def findAll = DB.withSession { 
    implicit session: Session =>
      (for (d <- Domains.sortBy(_.id) ) yield d).list
  }
    
  def findById(id: Int) = DB.withSession {
    implicit session: Session =>
      Try(Domains.byId(id).firstOption)
  }
  
  def delete(id: Int) = DB.withSession {
    implicit session: Session =>
      Try(Domains.where(_.id === id).delete)
  }
  
  def update(id: Int, domain: Domain) = DB.withSession {
    implicit session: Session => {
      val domainToUpdate = domain.copy(Some(id), domain.name, domain.master, Some(nowInUnixTime), domain.domainType, domain.notifiedSerial, domain.account)
      Try(Domains.where(_.id === id).update(domainToUpdate))
    }
  }
  
  def findPage(page: Int = 0, orderField: Int): Page[Domain] = {
    val offset = pageSize * page
    
    DB.withSession {
      implicit session: Session =>
        val domains = (
          for {d <- Domains.sortBy(d => orderField match {
            case 1 => d.id.asc
            case -1 => d.id.desc
            case 2 => d.name.asc
            case -2 => d.name.desc
          })
            .drop(offset)
            .take(pageSize)
          } yield d).list
          
          val totalRows = (for (d <- Domains) yield d.id).list.size
          Page(domains, page, offset, totalRows, pageSize)
    }
  }
  
  def insert(domain: Domain) = DB.withSession {
    implicit session: Session =>
      Try(Domains.autoInc.insert(domain))
  }
  
  implicit val recordFormat = Json.format[Domain]
}