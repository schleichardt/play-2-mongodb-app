package base

import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{MongodStarter, MongodProcess, MongodExecutable}
import de.flapdoodle.embed.process.distribution.IVersion
import de.flapdoodle.embed.process.runtime.Network
import org.specs2.execute.{Result, AsResult}
import org.specs2.mutable.Around
import org.specs2.specification.Scope
import reactivemongo.api.MongoConnection
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.test.{FakeApplication, WithApplication}
import collection.JavaConverters._

/** using https://github.com/flapdoodle-oss/embedmongo.flapdoodle.de */
abstract class WithMongoDb(val version: IVersion = Version.Main.V2_2,
                           val mongoDbPort: Int = Network.getFreeServerPort(),
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
    try {
      withMongoExe(runtime.prepare(new MongodConfig(version, mongoDbPort, isIPv6))) {
        AsResult(t)
      }
    } finally {
      connection.close()
    }
  }
  private lazy val connection = MongoConnection(List(s"localhost:$mongoDbPort"))
  lazy val db = connection(dbName)
  def dbName = "test"
}

abstract class WithMongoDbApplication(val application: FakeApplication = FakeApplication(), val mongo: WithMongoDb = new WithMongoDb() {}) extends Around with Scope {

  lazy val usedApplication = {
    val hostAndPort = "mongodb.servers" -> List( """localhost:%d""".format(mongo.mongoDbPort)).asJava
    val dbName = "mongodb.db" -> mongo.dbName
    val additionalConfiguration = Map(hostAndPort, dbName)
    application.copy(additionalConfiguration = additionalConfiguration)
  }

  override def around[T: AsResult](t: => T): Result = {
    mongo andThen (new WithApplication(usedApplication){}) around(t)
  }
}