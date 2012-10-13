import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "play-2-mongodb-app"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "se.radley" %% "play-plugins-salat" % "1.1"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    routesImport += "se.radley.plugin.salat.Binders._",
    templatesImport += "org.bson.types.ObjectId",
    ebeanEnabled := false
  ).configs(IntegrationTest)
    .settings(Defaults.itSettings: _*)
    .settings(
    libraryDependencies += "play" %% "play-test" % play.core.PlayVersion.current % "it"
    , scalaSource in IntegrationTest <<= baseDirectory / "it"
    , javaSource in IntegrationTest <<= baseDirectory / "it"
    , sourceDirectory in IntegrationTest <<= baseDirectory / "it"
  )
}
