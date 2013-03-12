import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "dns-management"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      // Add your Project dependencies here,
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own Project settings here      
    )

}
