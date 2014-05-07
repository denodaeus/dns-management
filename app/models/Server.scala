package models

import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.language.reflectiveCalls
import scala.util.Try

case class Server(
    id: Option[Int], 
    hostName: String, 
    ip: String
)

object Servers extends Table[Server]("servers"){
  import play.api.Play.current
  
  lazy val pageSize = 20
  
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def hostName = column[String]("hostname", O.NotNull)
  def ip = column[String]("ip", O.NotNull)
  def * = id.? ~ hostName ~ ip <> (Server, Server.unapply _)
  def autoInc = hostName ~ ip returning id
  
  val byId = createFinderBy(_.id)
  val byHostName = createFinderBy(_.hostName)
  val byIp = createFinderBy(_.ip)
  
  def findAll = DB.withSession {
    implicit session: Session =>
      (for (s <- Servers.sortBy(_.id)) yield s).list
  }
  
  def findById(id: Int) = DB.withSession {
    implicit session: Session =>
      Try(Servers.byId(id).firstOption)
  }
  
  def findPage(page: Int = 0, orderField: Int): Page[Server] = {
    val offset = pageSize * page
    
    DB.withSession {
      implicit session: Session =>
        val servers = (
          for {s <- Servers.sortBy(s => orderField match {
            case 1 => s.id.asc
            case -1 => s.id.desc
            case 2 => s.hostName.asc
            case -2 => s.hostName.desc
          })
            .drop(offset)
            .take(pageSize)
          } yield s).list
          
          val totalRows = (for (s <- Servers) yield s.id).list.size
          Page(servers, page, offset, totalRows, pageSize)
    }
  }
  
  def insert(s: Server) = DB.withSession { 
    implicit session: Session => 
      Try(Servers.autoInc.insert(s.hostName, s.ip))
  }
    
  def update(id: Int, server: Server) = DB.withSession { 
    implicit session: Session => {
      val serverToUpdate = server.copy(Some(id), server.hostName, server.ip)
      Logger.debug("update: updating serverid=" + id + ", server=" + server + ", previous=" + Servers.findById(id))
      Try(Servers.where(_.id === id).update(serverToUpdate))
    }
  }
  
  def delete(id: Int) = DB.withSession { 
    implicit session: Session => 
      Try(Servers.where(_.id === id).delete) 
  }

}