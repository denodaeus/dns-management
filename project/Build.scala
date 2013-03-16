import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "dns-management"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      filters,
      jdbc,
      "com.typesafe" % "play-slick_2.10" % "0.3.0",
      "com.wordnik" %% "swagger-play2" % "1.2.1-SNAPSHOT",
      "org.scalatest" %% "scalatest" % "1.9.1" % "test",
      "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(defaultScalaSettings:_*).settings(
      testOptions in Test := Nil,
      resolvers := Seq(
        "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
        "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
        "Maven Central" at "http://repo1.maven.org/maven2/",
        "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
         Resolver.url("Play Slick@ Github", url("http://loicdescotte.github.com/releases/"))(Resolver.ivyStylePatterns)
      )
    )

}
