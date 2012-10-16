package info.schleichardt.ic2.db.koans

import java.util.UUID
import org.specs2.execute._
import org.specs2.mutable._
import com.mongodb.casbah.Imports._
import play.api.Application
import info.schleichardt.ic2.db.DbTestTools._
import play.api.Play.current

class CasbahCrudSpec extends Specification {
  def createMongoDbObject() = MongoDBObject("eins" -> "1", "zwei" -> 2, "drei" -> 3.0)

  "with Casbah you" can {
    "store a single document" in {

      "a scala like syntax" in {
        runningMongoApp {
          withEmptyCollection {
            collection =>
              collection += createMongoDbObject()
              collection.size === 1
              collection.findOne().get.getAs[String]("eins").get === "1"
          }
        }
      }

      "a mongo like syntax" in {
        runningMongoApp {
          withEmptyCollection {
            collection =>
              collection.insert(createMongoDbObject())
              collection.size === 1
              collection.findOne().get.getAs[String]("eins").get === "1"
          }
        }
      }

    }

    "search a document" in {
      runningMongoApp {
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

    "retrieve data with all types of MongoDB" in {
      Pending("TODO http://www.mongodb.org/display/DOCS/Data+Types+and+Conventions and http://www.mongodb.org/display/DOCS/Advanced+Queries#AdvancedQueries-%24type")
    }
  }

  def emptyTestCollection()(implicit app: Application): MongoCollection = {
    val collection = salatPlugin.collection("collection_for_tests" + UUID.randomUUID().toString)
    collection.drop()
    collection.size === 0
    collection
  }


  def withEmptyCollection(block: MongoCollection => Result)(implicit app: Application) = {
    val collection = emptyTestCollection()
    try {
      block(collection)
    } finally {
      collection.drop()
    }
  }

  def withUserCollection(block: MongoCollection => Result)(implicit app: Application): Result = {
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
