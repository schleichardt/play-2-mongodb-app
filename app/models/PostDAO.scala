package models

import scala.concurrent.Future
import reactivemongo.api._
import reactivemongo.bson._
import handlers.{BSONWriter, BSONReader}
import play.api.Logger
import play.modules.reactivemongo._

import play.api.Play.current
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.dispatch.Futures
import org.joda.time.DateTime
import reactivemongo.api.QueryBuilder
import play.api.libs.functional.syntax._
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONReaderHandler

trait PostDAO {
  def obtain(limit: Int = 10): Future[Seq[Post]] //TODO use a query builder

  def byId(id: String): Future[Option[Post]]
}

object PostDAO extends PostDAO {
  def db = ReactiveMongoPlugin.db
  def collection = db("posts")

  implicit val postFormat = {
    def deserializer(id:String, title:String, content:String) = Post(id, title, content)
    def serializer(post: Post) = Post.unapply(post).map(item => (item._1, item._2, item._3)).get
    (
      (__ \ "_id").format[String] and
        (__ \ "title").format[String] and
        (__ \ "content").format[String]
      )(deserializer, serializer)
  }

  override def obtain(limit: Int) = {
    import play.modules.reactivemongo.PlayBsonImplicits._
    import reactivemongo.bson.handlers.DefaultBSONHandlers._
    val qb = QueryBuilder().query(Json.obj()).sort("published_at" -> SortOrder.Descending)
    collection.find[JsValue]( qb ).toList(upTo = limit).map { postsJson =>
      postsJson.map {element =>
        val post = Json.fromJson[Post](element).get
        println(post)
      post }
    }
  }

  def byId(id: String): Future[Option[Post]] = {
    implicit val reader = PostBSONReader
    id match {
      case s if !s.isEmpty => {
        val cursor = collection.find(BSONDocument("_id" -> BSONString(id)))
        cursor.headOption()
      }
      case _ => Futures.successful(None)
    }
  }

  object PostBSONReader extends BSONReader[Post] {
    def fromBSON(document: BSONDocument): Post = {
      val doc = document.toTraversable
      implicit def bsonStringToString(bsonString: BSONString) = bsonString.value
      implicit def bsonDateTimeToDateTime(bsonDateTime: BSONDateTime) = new DateTime(bsonDateTime.value)
      val comments = doc.getAs[TraversableBSONArray]("comments").map {
        _.toList.map(_.asInstanceOf[TraversableBSONDocument]).map {
          commentAsBSON =>
            for {
              author <- commentAsBSON.getAs[BSONString]("author")
              email <- commentAsBSON.getAs[BSONString]("email")
              content <- commentAsBSON.getAs[BSONString]("content")
              publishedAt <- commentAsBSON.getAs[BSONDateTime]("published_at")
            } yield Comment(author, email, content, publishedAt)
        }
      }.getOrElse(Nil).map(_.get)

      Post(
        doc.getAs[BSONString]("_id").get.value,
        doc.getAs[BSONString]("title").get.value,
        doc.getAs[BSONString]("content").get.value,
        comments
      )
    }
  }

  object PostBSONWriter extends BSONWriter[Post] {
    def toBSON(post: Post) = {
      val id: BSONString = (post.id) match {
        case "" => BSONString(BSONObjectID.generate.toString)
        case x => BSONString(x)
      }

      BSONDocument(
        "_id" -> id,
        "title" -> BSONString(post.title),
        "content" -> BSONString(post.content)
      )
    }
  }
}