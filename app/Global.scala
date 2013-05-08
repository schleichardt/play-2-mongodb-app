import info.schleichardt.play.embed.mongo.DynamicEmbedMongoPort
import play.api.{Logger, Application, GlobalSettings}
import scala.collection.JavaConverters._

object Global extends GlobalSettings with DynamicEmbedMongoPort {


  override def onStart(app: Application) {
    Logger.info("application starts")
  }

  override def additionalEmbedMongoPortSettings(port: Int) = Map("mongodb.servers" -> List(s"localhost:$port").asJava)

  override def onStop(app: Application) {
    Logger.info("application stops")
  }
}
