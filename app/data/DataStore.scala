package data

import models.{Record, Domain, Zone}
import scala.collection.mutable._
import java.util._
import java.util.Collection

object DataStore {
  
  var records = new java.util.HashMap[Int, Record]
  var domains = new java.util.HashMap[Int, Domain]
  var zone = new java.util.HashMap[Int, Zone]

}