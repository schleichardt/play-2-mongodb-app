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

class IntegrationSpec extends Specification {
  "Application" should {
    "be able to connect with MongoDB" in new WithApplication {
      val dbx = ReactiveMongoPlugin.db
      val collection = dbx("acoll")
      val cursor = collection.find(BSONDocument())
      await(cursor.toList).size === 0
    }

    "load initial data (Fixtures)" in new WithApplication {
      val dbx = ReactiveMongoPlugin.db
      val collection = dbx("posts")
      val cursor = collection.find(BSONDocument())
      val documents = await(cursor.toList)
      documents.size must beGreaterThan(1)
    }
  }
}

