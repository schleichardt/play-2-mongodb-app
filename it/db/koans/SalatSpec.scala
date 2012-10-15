package info.schleichardt.ic2.db.koans

import com.mongodb.casbah
import java.util.UUID
import org.specs2.execute._
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.mongodb.casbah.Imports._
import play.api.Application
import info.schleichardt.ic2.db.DbTestTools._
import com.novus.salat.global._
import com.novus.salat.annotations._
import com.mongodb.casbah.Imports._
import org.scala_tools.time.Imports._
import com.novus.salat.dao.{ SalatDAO, ModelCompanion }
import java.util
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.global._
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

//some code is from https://github.com/novus/salat/wiki/SalatWithPlay2

case class Person(firstName: String, lastName: String, yearOfBirth: Int)


class SalatSpec extends Specification {
  val michael = Person("Michael", "Schleichardt", 1984)

  "with Salat you" can {
    "export a case class to a DBObject" in {
      val databaseObject: DBObject = grater[Person].asDBObject(michael)
      val fields = databaseObject.toMap
      fields.get("firstName") must beEqualTo(michael.firstName)
      fields.get("lastName") must beEqualTo(michael.lastName)
      fields.get("yearOfBirth") must beEqualTo(michael.yearOfBirth)
    }

    "create a case class instance from a DBObject" in {
      //shows also how to create a DBObject from a Map
      val dbObject: DBObject = new BasicDBObject(Map("firstName" -> "Max", "lastName" -> "Mustermann", "yearOfBirth" -> 1970))
      val person: Person = grater[Person].asObject(dbObject)
      Person("Max", "Mustermann", 1970) mustEqual person
    }
  }
}