import sbt._
import Keys._
import play.Project._
import de.johoop.jacoco4sbt._
import JacocoPlugin._

object ApplicationBuild extends Build {

  val appName         = "play-2-mongodb-app"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.reactivemongo" %% "reactivemongo" % "0.8"
    , "org.reactivemongo" %% "play2-reactivemongo" % "0.8"
    , "junit" % "junit-dep" % "4.11" % "test"
    , "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "1.28"
    , "org.webjars" % "webjars-play" % "2.1.0"
    , "org.webjars" % "bootstrap" % "2.3.0"
  )

  lazy val jacocoSettings = jacoco.settings ++ Seq(
    parallelExecution in jacoco.Config := false
    , jacoco.excludes in jacoco.Config ~= { _ ++ Seq("**.ref.**", "**.Reverse*", "views.html.**", "Routes*", "controllers.routes**") }
    , testOptions in jacoco.Config += Tests.Argument("junitxml", "console")
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += Resolver.sonatypeRepo("snapshots")
    , testOptions in Test += Tests.Argument("junitxml", "console")
    , logBuffered in Test := false
  ).settings(jacocoSettings : _*)
}
