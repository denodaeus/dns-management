package models

import play.api.db.DB
import play.api.Play.current
import play.api.mvc.QueryStringBindable

import scala.slick.session.Database
import scala.slick.session.Database.threadLocalSession

/*case class Page[A](items: List[A], page: Int, offset: Long, total: Long) {
	lazy val prev = Option(page - 1).filter(_ >= 0)
	lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}*/