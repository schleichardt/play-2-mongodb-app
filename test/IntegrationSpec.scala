package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.functional.syntax.toApplicativeOps
import concurrent.{Await, Future}
import reactivemongo.core.commands.CollStatsResult
import reactivemongo.bson.{TraversableBSONDocument, BSONDocument}
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentReader

class IntegrationSpec extends Specification {
  "Application" should {
    "be able to connect with MongoDB" in {
      import reactivemongo.api._
      import scala.concurrent.ExecutionContext.Implicits.global
      import scala.concurrent.duration._

      val connection = MongoConnection( List( "localhost:27017" ) )
      val db = connection("plugin")
      val collection = db("acoll")
      val cursor = collection.find(BSONDocument())
      Await.result(cursor.toList, Duration("1 second")).size === 0
    }
  }
}
