package test

import org.specs2.mutable._

import concurrent.{Awaitable, Await}
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentReader
import base.WithMongoDB
import reactivemongo.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import play.api.test.WithApplication

import models.{Post, PostDAO}
import com.sun.corba.se.impl.orbutil.closure.Future

class IntegrationSpec extends Specification {
  def await[T](awaitable: Awaitable[T]) = Await.result(awaitable, Duration("2 seconds"))

  "Application" should {
    "be able to connect with MongoDB" in new WithMongoDB {
      val connection = MongoConnection( List( s"localhost:$port" ) )
      val db = connection("plugin")
      val collection = db("acoll")
      val cursor = collection.find(BSONDocument())
      await(cursor.toList).size === 0
      connection.close()
      1 === 1
    }

    "load initial data (Fixtures)" in new WithApplication {
      val allPosts = await(PostDAO.obtain())
      allPosts.size must beGreaterThan(0)
    }
  }
}

