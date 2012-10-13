import com.mongodb.casbah
import java.util.UUID
import org.specs2.mutable._
import org.specs2.specification._
import play.api.test._
import play.api.test.Helpers._
import se.radley.plugin.salat._
import com.mongodb.casbah.Imports._
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
        withEmptyCollection {
          collection =>
            val data = MongoDBObject("eins" -> "1", "zwei" -> 2, "drei" -> 3.0)
            collection += data

            collection.size === 1
            collection.findOne().get.getAs[String]("eins").get === "1"

            val db: casbah.MongoDB = salatPlugin.db()
            db.stats.ok === true
        }
      }
    }

    "search a document" in {
      implicit val app = FakeApplication()
      running(app) {
        withUserCollection {
          users =>
            val julia = users.findOne(MongoDBObject("firstname" -> "Julia")).get
            julia.getAs[String]("email").get === "julia@localhost"
            val usersWithEmail = users.find("email" $exists true)
            usersWithEmail.size === 2
            val emails: Iterator[String] = usersWithEmail.map(_.getAs[String]("email").get)
            val worked = emails.exists(_ == "max@localhost") && emails.exists(_ == "julia@localhost")
            //todo bug: "Der Dateiname ist zu lang" by comparing directly
            worked must beTrue
        }
      }
    }
  }

  def emptyTestCollection()(implicit app: Application): MongoCollection = {
    val collection = salatPlugin.collection("collection_for_tests" + UUID.randomUUID().toString)
    collection.drop()
    collection.size === 0
    collection
  }


  def withEmptyCollection(block: MongoCollection => Example)(implicit app: Application) = {
    val collection = emptyTestCollection()
    try {
      block(collection)
    } finally {
      collection.drop()
    }
  }

  def withUserCollection(block: MongoCollection => Example)(implicit app: Application) = {
    val collection = emptyTestCollection()
    collection += MongoDBObject("firstname" -> "Michael", "lastname" -> "Schleichardt")
    collection += MongoDBObject("firstname" -> "Max", "lastname" -> "Mustermann", "email" -> "max@localhost")
    collection += MongoDBObject("firstname" -> "Julia", "lastname" -> "Musterfrau", "email" -> "julia@localhost")
    try {
      block(collection)
    } finally {
      collection.drop()
    }
  }
}
