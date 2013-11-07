package models

case class Cluster(id: Option[Int], controllerHost: String, accounts: List[String])

object Cluster {
  
  def getClusterARecordsByServerId(id: Int): Seq[Record] = {
    val content = Servers.findById(id).get.ip
    Records.findByContent(content).sortBy(_.id)
  }
  
  def getClusterARecordCountByServerId(id: Int): Int = {
    getClusterARecordsByServerId(id).size
  }

}