package models

import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB

case class SqlAuditAction(statements: Seq[String], date: String, startSoa: Int, endSoa: Int)
case class AuditAction(id: Option[Int], action: String, date: String, startSoa: Int, endSoa: Int)

object AuditActions extends Table[AuditAction]("audit_actions") {
  import play.api.Play.current
  
  lazy val pageSize = 20
  
  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
  def date = column[String]("date", O.NotNull)
  def startSoa = column[Int]("start_soa", O.NotNull)
  def endSoa = column[Int]("end_soa", O.NotNull)
  def action = column[String]("action", O.NotNull)
  def * = id.? ~ action ~ date ~ startSoa ~ endSoa <> (AuditAction, AuditAction.unapply _)
  def autoInc = * returning id
}