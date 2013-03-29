import play.api.{Logger, Application, Configuration, GlobalSettings}
import plugins.embedmongo.EmbedMongoPlugin.reservePort
import scala.collection.JavaConverters._

object Global extends GlobalSettings{


  override def onStart(app: Application) {
    Logger.info("application starts")
  }

  override def configuration = {
    val embedmongoActive = super.configuration.getBoolean("embedmongo.enabled").getOrElse(true)
    if (embedmongoActive) {
      val port = reservePort()
      super.configuration ++
        Configuration.from(Map("embedmongo.port" -> port, "mongodb.servers" -> List(s"localhost:$port").asJava))
    } else super.configuration
  }

  override def onStop(app: Application) {
    Logger.info("application stops")
  }
}
