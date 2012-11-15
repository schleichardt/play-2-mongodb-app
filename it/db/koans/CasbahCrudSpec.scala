package info.schleichardt.ic2.db.koans


import com.mongodb.casbah.query.Imports.IntOk
import java.util.UUID
import org.specs2.execute._
import com.mongodb.casbah.Imports._
import org.specs2.mutable._
import play.api.Application
import info.schleichardt.ic2.db.DbTestTools._
import play.api.Play.current
import org.specs2.matcher.ResultMatchers
import play.api.test.Helpers._
import scala.Some
import play.api.test.FakeApplication
import org.specs2.execute.Pending

class CasbahCrudSpec extends Specification with ResultMatchers {
  "with Casbah you" can {

    case class RunAround(app: FakeApplication) extends Around {
      def around[R <% Result](r: =>R) = running(app)(r)
    }

    implicit val application = RunAround(FakeApplication())


    "store a single document a scala like syntax" in {
        withEmptyCollection {
          collection =>
            collection += createMongoDbObject()
            collection.size === 1
            collection.findOne().get.getAs[String]("eins").get === "1"
        }
    }

    "store a single document a mongo like syntax" in {
        withEmptyCollection {
          collection =>
            collection.insert(createMongoDbObject())
            collection.size === 1
            collection.findOne().get.getAs[String]("eins").get === "1"
        }
    }

    " store multiple documents (batch insert, fast)with explicit number of elements" in {
        withEmptyCollection {
          collection =>
            collection.insert(createMongoDbObject(), createMongoDbObject(), createMongoDbObject())
            collection.size === 3
        }
    }

    "store multiple documents (batch insert, fast) with unknown number of elements" in {
        withEmptyCollection {
          collection =>
            val list = List(createMongoDbObject(), createMongoDbObject(), createMongoDbObject())
            collection.insert(list: _*)
            collection.size === list.size
        }
    }

    "store multiple documents (batch insert, fast) does not have mongos 16MB per batch insert limitation" in {
      if (false) {
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
      } else skipped("runs too long")

    }

    "search a document" in {
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
        withUserCollection {
          users =>
            val sizeBefore = users.size
            users.remove(queryMax())
            users.size mustEqual sizeBefore - 1
        }
    }

    "drop an entire collection" in {
        withUserCollection {
          users =>
            users.size must be_>=(1)
            users.drop()
            users.size mustEqual 0
        }
    }

    "update documents using document replacement" in {//this is useful if you serialized the object and want do put the full object to the database
        withUserCollection {
          users =>
            val entireNewDocument = MongoDBObject("firstname" -> "NewName")
            users.find(queryMax).size === 1
            users.find(entireNewDocument).size === 0

            val selectStatement = queryMax
            val updateStatement = entireNewDocument
            users.update(selectStatement, updateStatement )
            users.find(queryMax).size === 0
            users.find(entireNewDocument).size === 1
            users.findOne(entireNewDocument).get.getAs[String]("email") === None //overriding may causes data loss
        }
    }

    "update documents using $set to update only some fields" in {
        withUserCollection {
          users =>
            val newEmail = 42
            users.update(queryMax(), $set ("email" -> newEmail /* here could be more tuples*/) ) //query can change type #email
            val max = users.findOne(queryMax()).get
            max.getAs[String]("lastname") must beSome(maxLastname) //lastname not changed or empty
            max.getAs[Int]("email") must beSome(newEmail)
        }
    }

    "update documents using $unset removes an attribute" in {
        withUserCollection {
          users =>
            users.update(queryMax(), $unset ("email") )
            val max = users.findOne(queryMax()).get
            max.getAs[Int]("email") must beNone
        }
    }

    "update documents using $inc is a fast version for number=number+x" in {//useful for counter
        withUserCollection {
          implicit users =>
            val newScoreAsAbsoluteValue = 50
            users.update(queryMax(), $inc("score" -> newScoreAsAbsoluteValue) )
            users.update(queryMichael(), $inc("score" -> newScoreAsAbsoluteValue) )
            val max = users.findOne(queryMax()).get
            max.getAs[Int]("score") must beSome(initialScoreMax + newScoreAsAbsoluteValue)
            val michael = obtainMichael()
            michael.getAs[Int]("score") must beSome(0 + newScoreAsAbsoluteValue)//creates field with initial value 0
        }
    }

    "update documents using $push to add an element at the end of an array" in {
        withUserCollection {
          implicit users =>
            users.update(queryMichael(), $push("comments" -> "test subject"))
            users.update(queryMichael(), $push("comments" -> "second comment"))
            obtainMichael().as[MongoDBList]("comments").size === 2
            obtainMichaelComments.apply(1) === "second comment"//be careful in lists it could be mixed type, AnyRef
        }
      /*
     if there should not be duplicates. you can query like that:
     {"comments": {"$ne" : "test subject"}}
      */
    }

    "update documents using $addToSet to prevent duplicate entries" in {
        withUserCollection {
          implicit users =>
            users.update(queryMichael(), $push("comments" -> "test subject"))
            users.update(queryMichael(), $addToSet("comments" -> "test subject"))
            users.update(queryMichael(), $addToSet("comments" -> "test subject"))
            users.update(queryMichael(), $addToSet("comments" -> "second comment"))
            users.update(queryMichael(), $addToSet("comments" -> "test subject"))
            users.update(queryMichael(), $addToSet("comments" -> "test subject"))
            obtainMichael().as[MongoDBList]("comments").size === 2
            obtainMichaelComments.apply(1) === "second comment"
        }
    }

    "update documents using $addToSet with $each for multi-value updates" in {
        withUserCollection {
          implicit users =>
            users.update(queryMichael(), $push("comments" -> "test subject"))
            users.update(queryMichael(), $push("comments" -> "bla bla"))
            users.update(queryMichael(), $push("comments" -> "second comment"))
            obtainMichaelComments.size === 3
            users.update(queryMichael(), $addToSet("comments") $each("test subject", "only this should be added", "bla bla", "second comment"))
            obtainMichaelComments.size === 4
            obtainMichaelComments.apply(3) === "only this should be added"
        }
    }

    "update documents using $pop to remove an array element at start or end" in {
        withUserCollection {
          implicit users =>
            add4CommentsToMichael()
            obtainMichaelComments.size === 4
            users.update(queryMichael(), $pop("comments" -> 1)) //last
            obtainMichaelComments.size === 3
            obtainMichaelComments.apply(0) === "a"
            obtainMichaelComments.apply(2) === "c"
            users.update(queryMichael(), $pop("comments" -> -1)) //first
            obtainMichaelComments.size === 2
            obtainMichaelComments.apply(0) === "b"
        }
    }

    "update documents using $pull to remove specific elements in an array" in {
        withUserCollection {
          implicit users =>
            add4CommentsToMichael()
            users.update(queryMichael(), $pull("comments" -> "c"))
            obtainMichaelComments.size === 3
            obtainMichaelComments must contain("a", "b", "d").inOrder
        }
    }

    "update documents using $pull to remove not existing element gives no warning" in {
        withUserCollection {
          implicit users =>
            add4CommentsToMichael()
            users.update(queryMichael(), $pull("comments" -> "not there"))
            obtainMichaelComments.size === 4
            obtainMichaelComments must contain("a", "b", "c", "d").inOrder
        }
    }

    "update documents using positional array modifications" in {
        def entry(number: Int) = MongoDBObject("number" -> number, "other" -> "other value")

        withUserCollection {
          implicit users =>
            users.update(queryMichael(), $addToSet("subarrays") $each(entry(0),entry(1),entry(2)))
            users.update(queryMichael(), $inc("subarrays.1.number" -> 13))
            obtainMichael().as[MongoDBList]("subarrays").getAs[BasicDBObject](1).get.getAs[Int]("number") === Some(14)
        }
    }
  }


  def add4CommentsToMichael()(implicit users: MongoCollection): WriteResult = {
    users.update(queryMichael(), $addToSet("comments") $each("a", "b", "c", "d"))
  }

  def obtainMichaelComments()(implicit users: MongoCollection): Seq[String] = {
    obtainMichael().as[MongoDBList]("comments").collect {
      case s: String => s
    }
  }

  def obtainMichael()(implicit users: MongoCollection): MongoDBObject = users.findOne(queryMichael()).get

  val maxLastname = "Mustermann"

  def createMongoDbObject() = MongoDBObject("eins" -> "1", "zwei" -> 2, "drei" -> 3.0)

  def queryMichael() = MongoDBObject("firstname" -> "Michael", "lastname" -> "Schleichardt")

  def fullQueryMax() = MongoDBObject("firstname" -> "Max", "lastname" -> maxLastname, "email" -> "max@localhost", "score" -> initialScoreMax)

  def queryMax() = MongoDBObject("firstname" -> "Max")
  val initialScoreMax = 25

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
    implicit val collection = emptyTestCollection()
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
