package plugins.embedmongo

import de.flapdoodle.embed.mongo.{MongodProcess, MongodExecutable, MongodStarter}
import play.api.{Logger, Plugin, Application}
import de.flapdoodle.embed.mongo.config.{RuntimeConfig, MongodConfig}
import de.flapdoodle.embed.process.runtime.Network
import de.flapdoodle.embed.process.distribution.GenericVersion
import java.util.logging.{Logger => JLogger}

/** provides a MongoDB instance for development and testing
  * Hast to be loaded before any other MongoDB related plugin.
  * */
class EmbedMongoPlugin(app: Application) extends Plugin {
  private var mongoExe: MongodExecutable = _
  private var process: MongodProcess = _

  override def enabled = app.configuration.getBoolean("embedmongo.enabled").getOrElse(false)

  override def onStart() {
    super.onStart()
    val runtimeConfig = RuntimeConfig.getInstance(JLogger.getLogger("embedmongo"))
    val runtime = MongodStarter.getInstance(runtimeConfig)
    val versionNumber = app.configuration.getString("embedmongo.dbversion").get
    val version = new GenericVersion(versionNumber)
    val port = app.configuration.getInt("embedmongo.port").get
    mongoExe = runtime.prepare(new MongodConfig(version, port, Network.localhostIsIPv6()))
    process = mongoExe.start()
    Logger.info(s"started embedmongo on port $port")
  }

  override def onStop() {
    super.onStop()
    try {
      if (mongoExe != null)
        mongoExe.stop()
    } finally {
      if (process != null)
        process.stop()
    }
  }
}

object EmbedMongoPlugin {
  def freePort() = Network.getFreeServerPort
}