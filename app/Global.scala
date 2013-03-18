import play.api.{Configuration, GlobalSettings}
import plugins.embedmongo.EmbedMongoPlugin.freePort
import scala.collection.JavaConverters._

object Global extends GlobalSettings{
  override def configuration = {
    val embedmongoActive = super.configuration.getBoolean("embedmongo.enabled").getOrElse(true)
    if (embedmongoActive) {
      val port = freePort()
      super.configuration ++
        Configuration.from(Map("embedmongo.port" -> port, "mongodb.servers" -> List(s"localhost:$port").asJava))
    } else super.configuration
  }
}
