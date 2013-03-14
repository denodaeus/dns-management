package controllers

import models._

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.i18n.Messages
import play.api.data.Forms.{mapping, longNumber, nonEmptyText}

import play.api.data.format.Formats._
import play.api.libs.json._


object Records extends Controller {
  implicit object RecordFormat extends Format[Record] {
    def writes(r: Record): JsValue = JsObject(
      List("id" -> JsNumber(r.id),
          "domainId" -> JsNumber(r.domainId),
          "name" -> JsString(r.name),
          "recordType" -> JsString(r.recordType.toString()),
          "content" -> JsString(r.content),
          "ttl" -> JsNumber(r.ttl),
          "priority" -> JsNumber(r.priority),
          "changeDate" -> JsNumber(r.modified)
      )
    )
    
    def reads(json: JsValue): JsResult[Record] = JsSuccess(Record(
      (json \ "id").as[Int],
      (json \ "domainId").as[Int],
      (json \ "name").as[String],
      (json \ "recordType").as[String],
      (json \ "content").as[String],
      (json \ "ttl").as[Int],
      (json \ "priority").as[Int],
      (json \ "changeDate").as[Int]
    ))
  }
  
  def create = Action { implicit request =>
    val json = "create"
    Ok(json) as JSON
  }
  
  def read = Action { implicit request =>
    val json = "read"
    Ok(json) as JSON
  }
  
  def update = Action { implicit request =>
    val json = "update"
    Ok(json) as JSON
  }
  
  def delete = Action { implicit request =>
    val json = "delete"
    Ok(json) as JSON
  }
}