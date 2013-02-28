package base

import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{MongodStarter, MongodProcess, MongodExecutable}
import de.flapdoodle.embed.process.distribution.IVersion
import de.flapdoodle.embed.process.runtime.Network
import org.specs2.execute.{Result, AsResult}
import org.specs2.mutable.Around
import org.specs2.specification.Scope

/** using https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de */
abstract class WithMongoDB(val version: IVersion = Version.Main.PRODUCTION,
                           val port: Int = Network.getFreeServerPort(),
                           val isIPv6: Boolean = Network.localhostIsIPv6()
                           ) extends Around with Scope {
  private def withMongoExe[T: AsResult](mongoExe: MongodExecutable)(t: => T) = {
    try {
      withMongod(mongoExe.start()) {
        t
      }
    } finally {
      mongoExe.stop()
    }
  }

  private def withMongod[T: AsResult](process: MongodProcess)(t: => T) = {
    try {
      t
    } finally {
      process.stop()
    }
  }

  override def around[T: AsResult](t: => T): Result = {
    val runtime = MongodStarter.getDefaultInstance()
    withMongoExe(runtime.prepare(new MongodConfig(version, port, isIPv6))) {
      AsResult(t)
    }
  }
}
