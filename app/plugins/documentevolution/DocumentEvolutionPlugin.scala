package plugins.documentevolution

import play.api.{Logger, Plugin, Application}
import reactivemongo.bson._
import play.modules.reactivemongo.ReactiveMongoPlugin
import concurrent.Future
import play.api.libs.iteratee.Iteratee
import reactivemongo.bson.handlers.DefaultBSONHandlers._
import concurrent.ExecutionContext.Implicits.global

class DocumentEvolutionPlugin(implicit app: Application) extends Plugin {
  override def onStart() {
    new Migration1to2().apply()
  }
}

private[documentevolution] trait Migration {
  /** the query to obtain all documents that have to be migrated */
  def query: BSONDocument
  def migrateSingleDocument(doc: TraversableBSONDocument): Unit
  def collectionName: String
  implicit val application: Application
  lazy val collection = ReactiveMongoPlugin.db.apply(collectionName)
  def apply(): Future[Iteratee[TraversableBSONDocument, Unit]] = {
    Logger.info("applying " + this.getClass.getCanonicalName)
    collection.find(query).enumerate.apply(Iteratee.foreach(migrateSingleDocument))
  }
}

private[documentevolution] class Migration1to2(implicit val application: Application) extends Migration {
  override def migrateSingleDocument(doc: TraversableBSONDocument) {
    val modifier = BSONDocument(
      "$unset" -> BSONDocument(
        "postcode" -> new BSONInteger(1),
        "street" -> new BSONInteger(1),
        "city" -> new BSONInteger(1)
      ),
      "$set" -> BSONDocument("address" -> extractAddress(doc))
    )
    val query = BSONDocument("_id" -> doc.getAs[BSONString]("_id"))
    collection.update(query, modifier)
  }
  def extractAddress(doc: TraversableBSONDocument): Option[AppendableBSONDocument] = {
    for {
      street <- doc.getAs[BSONString]("street").map(_.value)
      postcode <- doc.getAs[BSONString]("postcode").map(_.value)
      city <- doc.getAs[BSONString]("city").map(_.value)
    } yield BSONDocument("street" -> BSONString(street), "postcode" -> BSONString(postcode), "city" -> BSONString(city))
  }
  override def query = BSONDocument("street" -> BSONDocument("$exists" -> new BSONBoolean(true)))
  override def collectionName = "persons"
}





