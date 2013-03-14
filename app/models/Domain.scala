package models

import play.api.Play.current
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Domain (
    id: Int,
    name: String,
    master: String,
    lastCheck: Long,
    domainType: String,
    notifiedSerial: Long
)

object DomainType extends Enumeration {
  type Type = Value
  val MASTER, SLAVE, NATIVE = Value
}

object Domain {
}