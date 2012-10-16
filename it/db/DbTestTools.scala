package info.schleichardt.ic2.db

import play.api.Application
import se.radley.plugin.salat.SalatPlugin
import org.specs2.execute.Result
import play.api.test.FakeApplication
import play.api.test.Helpers._
import play.api.test.FakeApplication

object DbTestTools {
  def salatPlugin(implicit app: Application): SalatPlugin = app.plugin(classOf[SalatPlugin]).get

  def runningMongoApp(block: => Result) = {
    val app = FakeApplication()
    running(app) {
      block
    }
  }
}