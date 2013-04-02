package utils

object DateTimeUtils {
  def nowInUnixTime = { (System.currentTimeMillis / 1000L).toInt }
}