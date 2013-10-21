import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.SbtNativePackager._

object Packaging {
  
  val settings: Seq[Setting[_]] = packagerSettings ++ deploymentSettings ++ Seq(
    name in Rpm := "dns-management",
    version in Rpm := "" + new java.util.Date().getTime,
    packageSummary in Linux := "DNS Management Tool for PowerDNS",
    rpmRelease := "1",
    rpmVendor := "Vocalocity",
    rpmRequirements ++= Seq("chkconfig", "java-1.7.0-openjdk-devel"),
    rpmPost := Option("""service dns-management stop service dns-management start"""),
    rpmLicense := Some("Proprietary"),

    linuxPackageMappings <+= (target) map { bd =>
      println("target " + bd)
      (packageMapping((bd / "scala-2.10/sbt-rpm_2.10-1.0.war") -> "/usr/local/dns-management/dns-management.war") 
       withUser "root" withGroup "root" withPerms "0755")
    }
  )
}
