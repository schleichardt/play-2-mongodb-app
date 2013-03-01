package test

import org.specs2.mutable._

import concurrent.Await
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentReader
import base.WithMongoDB
import reactivemongo.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class IntegrationSpec extends Specification {
  "Application" should {
    "be able to connect with MongoDB" in new WithMongoDB {
      val connection = MongoConnection( List( s"localhost:$port" ) )
      val db = connection("plugin")
      val collection = db("acoll")
      val cursor = collection.find(BSONDocument())
      Await.result(cursor.toList, Duration("2 seconds")).size === 0
      connection.close()
    }
  }
}

