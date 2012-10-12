import org.specs2.mutable._
import org.specs2.specification._
import play.api.test._
import play.api.test.Helpers._
import se.radley.plugin.salat._
import com.mongodb.casbah.Imports._
import com.novus.salat._
import play.api.Application

class DbConnectionSpec extends Specification {
  def salatPlugin(implicit app: Application): SalatPlugin = app.plugin(classOf[SalatPlugin]).get

  "The application" can {
    "start with salat plugin" in {
      implicit val app = FakeApplication()
      running(app) {
        salatPlugin.enabled === true
      }
    }

    "connect to the MongoDB" in {
      implicit val app = FakeApplication()
      running(app) {
        salatPlugin.db().stats.ok === true
      }
    }
  }
}
