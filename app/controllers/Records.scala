package controllers

import scala.math.BigDecimal.int2bigDecimal

import models.Record
import models.current.dao.RecordTable
import play.api.Play.current
import play.api.db.slick.DB
import play.api.libs.json.Format
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsResult
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller

object Records extends Controller {
  import play.api.Play.current
  
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
  
  val record = new Record(1, 1, "new record", "A", "foo.bar.com", 600, 1, 123456)
  val record2 = new Record(2, 2, "second    ", "A", "foo.bar.com", 600, 1, 12345)
  val records = (record, record2)
  
  def listAll = Action { implicit request =>
    val json = Json.arr("GET /records -> Records.listAll", record, record2)
    Ok(json) as JSON
  }
  
  def create = Action { implicit request =>
    val json = Json.arr("POST /records -> Records.create", record, record2)
    Ok(json) as JSON
  }
  
  def updateAll = Action { implicit request =>
    val json = Json.arr("PUT /records -> Records.updateAll", record, record2)
    Ok(json) as JSON
  }
  
  def deleteAll = Action { implicit request =>
    val json = Json.arr("DELETE /records -> Records.deleteAll", record, record2)
    Ok(json) as JSON
  }
  
  def get(id: Int) = Action { implicit request =>
    val record = Record.findById(id)
    val json = Json.arr(s"GET /records/$id -> Records.get $id", record)
    Ok(json) as JSON
  }
  
  def updateIfExists(id: Int) = Action { implicit request =>
    val json = Json.arr(s"PUT /records/$id -> Records.updateIfExists $id", record)
    Ok(json) as JSON
  }
  
  def deleteOne(id: Int) = Action { implicit request =>
    val json = Json.arr(s"DELETE /records/$id -> Records.deleteOne $id", record)
    Ok(json) as JSON
  }
}