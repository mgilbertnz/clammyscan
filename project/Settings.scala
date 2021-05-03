import sbt.Keys._
import sbt._

// scalastyle:off
object Settings {

  val ScalacOpts = Seq(
    "-encoding",
    "utf-8",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-explaintypes",
    "-Xfatal-warnings",
    "-Xlint",
//    "-Ywarn-adapted-args",
    "-Ywarn-dead-code",
//    "-Ywarn-inaccessible",
//    "-Ywarn-nullary-override",
    "-Ywarn-numeric-widen",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps"
  )

  val ScalaVersion = "2.13.5"

  val BaseSettings = Seq(
    scalaVersion := ScalaVersion,
    scalacOptions := ScalacOpts,
    organization := "com.mattgilbert",
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    Test / scalacOptions ++= Seq("-Yrangepos"),
    Test / logBuffered := false,
    Test / fork := true,
    Test / javaOptions += "-Dlogger.resource=logback-test.xml"
  )

  val NoPublish = Seq(
    publish := {},
    publishLocal := {}
  )

  val BintrayPublish = Seq(
    publishMavenStyle := true,
    Test / publishArtifact := false,
    pomExtra := (
      <url>https://github.com/mgilbertnz/clammyscan</url>
        <scm>
          <url>git@github.com:mgilbertnz/clammyscan.git</url>
          <connection>scm:git:git@github.com:mgilbertnz/clammyscan.git</connection>
        </scm>
        <developers>
          <developer>
            <id>kpmeen</id>
            <name>Knut Petter Meen</name>
            <url>http://scalytica.net</url>
          </developer>
        </developers>
    )
  )

  val FPPublish = Seq(
    resolvers += "Nexus Releases" at "https://nexus.financialplatforms.co.nz/repository/releases",
    resolvers += "Nexus Snapshots" at "https://nexus.financialplatforms.co.nz/repository/snapshots",
    publishTo := {
      val nexus = "https://nexus.financialplatforms.co.nz/repository/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "snapshots")
      else
        Some("releases" at nexus + "releases")
    },
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials.fp"),
    publishMavenStyle := true
  )

  def ClammyProject(name: String, folder: Option[String] = None): Project = {
    val folderName = folder.getOrElse(name)
    Project(name, file(folderName))
      .settings(BaseSettings: _*)
      .settings(
        updateOptions := updateOptions.value.withCachedResolution(true)
      )
      .settings(resolvers ++= Dependencies.Resolvers)
  }

}
// scalastyle:on
