package controllers

import models.Cluster
import models.Account
import models.Record
import play.Logger
import play.api.data.Form
import play.api.data.Forms.longNumber
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.data.Forms.number
import play.api.data.Forms.optional
import play.api.data.Forms.text
import play.api.data.Forms.list
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toContraFunctorOps
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller

object Clusters extends Controller with Secured {
  import play.api.Play.current

  implicit val recordWrites = Json.writes[Record]
  implicit val accountWrites = Json.writes[Account]
  implicit val writes = Json.writes[Cluster]

  val clusterForm = Form(
    mapping(
      "id" -> optional(number),
      "controllerHost" -> text,
      "accounts" -> list(text)
    ) (Cluster.apply)(Cluster.unapply)
  )

  def index = withAuth { username =>
    implicit request =>
      Ok(views.html.clusters.list(List()))
  }

  def newCluster() = withAuth { username =>
    implicit request =>
      Ok(views.html.clusters.create(clusterForm))
  }
  
  def create() = withAuth { username =>
    implicit request =>
  	Ok(views.html.clusters.index())
  }
  
  def listAll() = withAuth { username =>
    implicit request =>
      Ok(views.html.clusters.list(List()))
  }

}