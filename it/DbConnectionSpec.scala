import com.mongodb.casbah
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

    "store a document" in {
      implicit val app = FakeApplication()
      running(app) {


        val collection: MongoCollection = salatPlugin.collection("casbah_test")
        collection.drop()
        collection.size === 0

        val data = MongoDBObject("eins" -> "1", "zwei" -> 2, "drei" -> 3.0)
        collection += data

        collection.size === 1
        collection.findOne().get.getAs[String]("eins").get === "1"

        val db: casbah.MongoDB = salatPlugin.db()
        db.stats.ok === true
      }
    }
  }
}
