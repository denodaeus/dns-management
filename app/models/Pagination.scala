package models

import play.api.db.DB
import play.api.Play.current
import play.api.mvc.QueryStringBindable

import scala.slick.session.Database
import scala.slick.session.Database.threadLocalSession

case class Page[+A](items: Seq[A], page: Int, offset: Long, total: Long, pageSize: Int) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
  lazy val maxPages = (total.toDouble / pageSize).ceil.toInt
  lazy val paginationStart = (page - 2).max(1)
  lazy val paginationEnd = (page + 3).min(maxPages)
}

trait Pagination {
  val DEFAULT_OFFSET = 0;
  val DEFAULT_PAGE = 1;
  val DEFAULT_PAGESIZE = 25;
}