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

case class Account(id: Int, records: List[Record])

object Accounts {
  import play.api.Play.current

  lazy val pageSize = 20
  
  def findById(id: Int) = DB.withSession {
    implicit session: Session => {
      Try(Records.byAccountId(id).list) 
    }
  }
  
  def getAccount(id: Int): Account = {
    findById(id) match {
      case Success(records) => Account(id, records)
      case Failure(records) => Account(id, List[Record]())
    }
  }
  
  def findAll() = DB.withSession {
    implicit session: Session => Records.listAccountIds
  }
  
  def findPage(page: Int, orderBy: Int) = DB.withSession {
    implicit session: Session => {
      val offset = pageSize * page
      (models.Records.listAccountIds).drop(page).take(offset)
    }
  }

  implicit val recordFormat = Records.recordFormat
  implicit val accountFormat = Json.format[Account]
  
}