package models

import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.Profile

case class Domain (
    id: Int,
    name: String,
    master: String,
    lastCheck: Long,
    domainType: String,
    notifiedSerial: Long,
    account: String
)

object DomainType extends Enumeration {
  type Type = Value
  val MASTER, SLAVE, NATIVE = Value
}

object Domain extends DomainDAO {
}

trait DomainDAO {
  object DomainTable extends Table[Domain]("domain") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey, O.NotNull)
    def name = column[String]("name", O.NotNull)
    def master = column[String]("master", O.Default(null))
    def lastCheck = column[Long]("last_check")
    def domainType = column[String]("type", O.NotNull)
    def notifiedSerial = column[Long]("notified_serial")
    def account = column[String]("provider", O.Default(null))
    def * = id ~ name ~ master ~ lastCheck ~ domainType ~ notifiedSerial ~ account <> (Domain.apply _, Domain.unapply _)
  }
}