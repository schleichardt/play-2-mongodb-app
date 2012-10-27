package info.schleichardt.ic2.db.koans

import java.util.UUID
import org.specs2.execute._
import org.specs2.mutable._
import com.mongodb.casbah.Imports._
import play.api.Application
import info.schleichardt.ic2.db.DbTestTools._
import play.api.Play.current
import org.specs2.matcher.ResultMatchers

class CasbahCrudSpec extends Specification with ResultMatchers {
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

    "store multiple documents (batch insert, fast)" in {
      "with explicit number of elements" in {
        runningMongoApp {
          withEmptyCollection {
            collection =>
              collection.insert(createMongoDbObject(), createMongoDbObject(), createMongoDbObject())
              collection.size === 3
          }
        }
      }

      "with unknown number of elements" in {
        runningMongoApp {
          withEmptyCollection {
            collection =>
              val list = List(createMongoDbObject(), createMongoDbObject(), createMongoDbObject())
              collection.insert(list: _*)
              collection.size === list.size
          }
        }
      }

      "does not have mongos 16MB per batch insert limitation" in {
        if (false) {
          runningMongoApp {
            withEmptyCollection {
              collection =>
                val oneMegabyteInBytes = 1048576
                val limit = 16 * oneMegabyteInBytes
                val bytesPerObject = 10
                val objectNumberToExceedLimit = limit / bytesPerObject + 1
                val manyObjects = for (i <- 0 until objectNumberToExceedLimit) yield MongoDBObject("x" -> "123456789")
                collection.insert(manyObjects: _*)
                collection.size === manyObjects.size
            }
          }
        } else skipped("runs too long")

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

    "combine MongodDBObjects and the last setting wins" in {
      val combined1 = MongoDBObject("eins" -> "1") ++ MongoDBObject("zwei" -> "2")

      combined1.getAs[String]("eins") must beSome("1")
      combined1.getAs[String]("zwei") must beSome("2")

      val combined2 = combined1 ++ MongoDBObject("eins" -> "one")
      combined2.getAs[String]("eins") must beSome("one")
      combined2.getAs[String]("zwei") must beSome("2")
    }

    "delete some elements of a collection" in {
      runningMongoApp {
        withUserCollection {
          users =>
            val sizeBefore = users.size
            users.remove(queryMax())
            users.size mustEqual sizeBefore - 1
        }
      }
    }

    "drop an entire collection" in {
      runningMongoApp {
        withUserCollection {
          users =>
            users.size must be_>=(1)
            users.drop()
            users.size mustEqual 0
        }
      }
    }

    "update documents" in {
      "using document replacement" in {//this is useful if you serialized the object
        runningMongoApp {
          withUserCollection {
            users =>
              val newNameCriteria = MongoDBObject("firstname" -> "NewName")
              users.find(queryMax).size === 1
              users.find(newNameCriteria).size === 0

              val selectStatement = queryMax
              val updateStatement = newNameCriteria
              users.update(selectStatement, updateStatement )
              users.find(queryMax).size === 0
              users.find(newNameCriteria).size === 1
              users.findOne(newNameCriteria).get.getAs[String]("email") === None //overriding may causes data loss
          }
        }
      }

      "using $set" in {
          runningMongoApp {
            withUserCollection {
              users =>
                val newEmail = 42
                users.update(queryMax(), $set ("email" -> newEmail) ) //query can change type #email
                val max = users.findOne(queryMax()).get
                max.getAs[String]("lastname") must beSome(maxLastname) //lastname not changed or empty
                max.getAs[Int]("email") must beSome(newEmail)
            }
          }
      }
    }
  }

  val maxLastname = "Mustermann"

  def createMongoDbObject() = MongoDBObject("eins" -> "1", "zwei" -> 2, "drei" -> 3.0)

  def queryMichael() = MongoDBObject("firstname" -> "Michael", "lastname" -> "Schleichardt")

  def fullQueryMax() = MongoDBObject("firstname" -> "Max", "lastname" -> maxLastname, "email" -> "max@localhost")

  def queryMax() = MongoDBObject("firstname" -> "Max")

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
    collection += queryMichael
    collection += fullQueryMax
    collection += MongoDBObject("firstname" -> "Julia", "lastname" -> "Musterfrau", "email" -> "julia@localhost")
    try {
      block(collection)
    } finally {
      collection.drop()
    }
  }
}
