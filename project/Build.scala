import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "play-2-mongodb-app"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.reactivemongo" %% "reactivemongo" % "0.8"
    , "org.reactivemongo" %% "play2-reactivemongo" % "0.8"
    , "junit" % "junit-dep" % "4.11" % "test"
    , "org.webjars" % "webjars-play" % "2.1.0"
    , "org.webjars" % "bootstrap" % "2.2.2-1" //2.3.0 and 2.3.1 freeze the tests
    , "joda-time" % "joda-time" % "2.2"
    , "info.schleichardt" %% "play-embed-mongo" % "0.2"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += Resolver.sonatypeRepo("snapshots")
    , testOptions in Test += Tests.Argument("junitxml", "console")
    , logBuffered in Test := false
    , templatesImport ~= {current => current ++ Seq("views.TemplateUtil._")}
    , parallelExecution in Test := false
  )
}
