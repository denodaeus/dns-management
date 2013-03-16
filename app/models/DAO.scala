package models

import slick.driver.ExtendedProfile
import play.api.db.slick.Profile
import play.api.db.slick.DB
import play.api._

class DAO(override val profile: ExtendedProfile) extends RecordDAO with DomainDAO with Profile

object current {
  val dao = new DAO(DB.driver(Play.current))
}