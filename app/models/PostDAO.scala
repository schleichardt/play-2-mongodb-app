package models

import scala.concurrent.Future
import reactivemongo.api._
import reactivemongo.bson._
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import play.modules.reactivemongo._
import play.modules.reactivemongo.PlayBsonImplicits._
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.ExecutionContext.Implicits.global

trait PostDAO {
  def obtain(limit: Int = 10): Future[Seq[Post]] //TODO use a query builder
}

object PostDAO extends PostDAO {
  val db = ReactiveMongoPlugin.db
  lazy val collection = db("posts")

  override def obtain(limit: Int) = {
    import play.api.libs.json._
    import play.api.libs.functional.syntax._
    implicit val postFormat = (
      (__ \ "title").format[String] and
        (__ \ "content").format[String]
      )(Post.apply, unlift(Post.unapply))

    val qb = QueryBuilder().query(Json.obj()).sort("published_at" -> SortOrder.Descending)
    collection.find[JsValue]( qb ).toList(upTo = limit).map { postsJson =>
      postsJson.map {element => Json.fromJson[Post](element).get }
    }
  }
}