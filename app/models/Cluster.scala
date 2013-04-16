package models

case class Cluster(id: Int, controllerHost: String, accounts: List[Account])

object Cluster {

}