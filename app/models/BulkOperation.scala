package models

import play.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class BulkAccountOperation(operation: String, accounts: Seq[Int])
case class BulkOperation(operation: String, accounts: String)
case class BulkCreateOperation(accounts: String, records: Seq[BasicRecord])

object BulkOperation {
  
  def parseAccountsToSeq(accounts: String, delimiter: Char): Seq[Int] = {
    val list = accounts.split(delimiter).map(_.toInt)
    Logger.debug(s"parseAccountsToSeq :: parsed accounts $list")
    list
  }
  
}