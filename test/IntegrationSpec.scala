package test

import org.specs2.mutable._

import concurrent.{Awaitable, Await}
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import reactivemongo.bson._

import play.modules.reactivemongo._
import play.modules.reactivemongo.PlayBsonImplicits._
import play.api.test.WithApplication
import test.TestUtil._
import play.api.test.Helpers._
import play.api.libs.json.JsValue
import play.api.libs.json.Json

class IntegrationSpec extends Specification {
  "Application" should {
    "load initial data (Fixtures)" in new WithApplication {
      val dbx = ReactiveMongoPlugin.db
      val collection = dbx("posts")
      val cursor = collection.find(BSONDocument())
      val documents = await(cursor.toList)
      documents.size must beGreaterThan(1)
    }

    "migrate the json structure" in new WithApplication {
      val dbx = ReactiveMongoPlugin.db
      val collection = dbx("persons")
      val cursor = collection.find(BSONDocument("_id" -> BSONString("McGarrett")))
      val documents = await(cursor.toList)
      documents.size must beEqualTo(1)
      val mcgarrett: JsValue = documents(0)

      (mcgarrett \ "name").as[String] === "McGarrett"
      (mcgarrett \ "address" \ "street").as[String] === "Mellenseestrasse 4"
      (mcgarrett \ "address" \ "city").as[String] === "Berlin"
    }
  }
}

