package models

case class Cluster(id: Option[Int], controllerHost: String, accounts: List[String])

object Cluster {

}