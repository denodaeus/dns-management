package controllers

import models.Zone

import play.api.data.Form
import play.api.data.Forms.{mapping, longNumber, nonEmptyText}
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._
import play.api.i18n.Messages

object Zones extends Controller {

  implicit object ZoneFormat extends Format[Zone] {
    def writes(z: Zone): JsValue = JsObject(
      List("id" -> JsNumber(z.id),
          "domainName" -> JsString(z.domainName),
          "description" -> JsString(z.description),
          "lastModified" -> JsNumber(z.lastModified)
      )
    )
    
    def reads(json: JsValue): JsResult[Zone] = JsSuccess(Zone(
      (json \ "id").as[Int],
      (json \ "domainName").as[String],
      (json \ "description").as[String],
      (json \ "lastModified").as[Long]
    ))
  }
  
  val zone = new Zone(1, "ZH1BK2542", "vocalocity.com", 2123145525)
  val zone2 = new Zone(1, "ZH1BK2542", "vocal-dev.com", 2123145525)
  val zones = (zone, zone2)
  
  def listAll = Action { implicit request =>
    val json = Json.arr("GET /zones -> Zones.listAll", zone, zone2)
    Ok(json) as JSON
  }
  
  def create = Action { implicit request =>
    val json = Json.arr("POST /zones -> Zones.create", zone, zone2)
    Ok(json) as JSON
  }
  
  def updateAll = Action { implicit request =>
    val json = Json.arr("PUT /zones -> Zones.updateAll", zone, zone2)
    Ok(json) as JSON
  }
  
  def deleteAll = Action { implicit request =>
    val json = Json.arr("DELETE /zones -> Zones.deleteAll", zone, zone2)
    Ok(json) as JSON
  }
  
  def get(id: Int) = Action { implicit request =>
    val json = Json.arr(s"GET /zones/$id -> Zones.get $id", zone)
    Ok(json) as JSON
  }
  
  def updateIfExists(id: Int) = Action { implicit request =>
    val json = Json.arr(s"PUT /zones/$id -> Zones.updateIfExists $id", zone)
    Ok(json) as JSON
  }
  
  def deleteOne(id: Int) = Action { implicit request =>
    val json = Json.arr(s"DELETE /zones/$id -> Zones.deleteOne $id", zone)
    Ok(json) as JSON
  }

}