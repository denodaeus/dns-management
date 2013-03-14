package controllers

import models.Domain

import play.api.data.Form
import play.api.data.Forms.{mapping, longNumber, nonEmptyText}
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._
import play.api.i18n.Messages

object Domains extends Controller {

  implicit object DomainFormat extends Format[Domain] {
    def writes(d: Domain): JsValue = JsObject(
      List("id" -> JsNumber(d.id),
          "name" -> JsString(d.name),
          "master" -> JsString(d.master),
          "lastCheck" -> JsNumber(d.lastCheck),
          "type" -> JsString(d.domainType),
          "notifiedSerial" -> JsNumber(d.notifiedSerial)
      )
    )
    
    def reads(json: JsValue): JsResult[Domain] = JsSuccess(Domain(
      (json \ "id").as[Int],
      (json \ "name").as[String],
      (json \ "master").as[String],
      (json \ "lastCheck").as[Long],
      (json \ "type").as[String],
      (json \ "notifiedSerial").as[Long]
    ))
  }
  
  val domain = new Domain(1, "accounts.vocalocity.com", "atlsvppdns01.atldc.vocalocity.com", 2123145525, "SLAVE", 600)
  val domain2 = new Domain(1, "media.vocalocity.com", "atlsvppdns01.atldc.vocalocity.com", 2123145525, "SLAVE", 600)
  val domains = (domain, domain2)
  
  def listAll = Action { implicit request =>
    val json = Json.arr("GET /domains -> Domains.listAll", domain, domain2)
    Ok(json) as JSON
  }
  
  def create = Action { implicit request =>
    val json = Json.arr("POST /domains -> Domains.create", domain, domain2)
    Ok(json) as JSON
  }
  
  def updateAll = Action { implicit request =>
    val json = Json.arr("PUT /domains -> Domains.updateAll", domain, domain2)
    Ok(json) as JSON
  }
  
  def deleteAll = Action { implicit request =>
    val json = Json.arr("DELETE /domains -> Domains.deleteAll", domain, domain2)
    Ok(json) as JSON
  }
  
  def get(id: Int) = Action { implicit request =>
    val json = Json.arr(s"GET /domains/$id -> Domains.get $id", domain)
    Ok(json) as JSON
  }
  
  def updateIfExists(id: Int) = Action { implicit request =>
    val json = Json.arr(s"PUT /domains/$id -> Domains.updateIfExists $id", domain)
    Ok(json) as JSON
  }
  
  def deleteOne(id: Int) = Action { implicit request =>
    val json = Json.arr(s"DELETE /domains/$id -> Domains.deleteOne $id", domain)
    Ok(json) as JSON
  }
}