import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "play-2-mongodb-app"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.reactivemongo" %% "reactivemongo" % "0.9-SNAPSHOT"
    , "junit" % "junit-dep" % "4.11"
    , "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "1.28"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += Resolver.sonatypeRepo("snapshots")
  )

}
