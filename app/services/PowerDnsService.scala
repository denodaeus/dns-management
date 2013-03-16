package services

import models._
import play.api.db.slick.Profile
import play.api.db.slick.Config.driver.simple._


trait PowerDnsService { self =>
  
  case class PdnsRecord(
    id: Option[Int],
    domainId: Option[Int],
    name: Option[String],
    recordType: Option[String],
    content: Option[String],
    ttl: Option[Int],
    prio: Option[Int],
    changeDate: Option[Int]
  )
  
  case class PdnsDomain()
  case class PdnsZone()

}