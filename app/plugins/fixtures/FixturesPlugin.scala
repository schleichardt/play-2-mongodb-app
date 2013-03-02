package plugins.fixtures

import play.api.{Logger, Plugin, Application}
import play.modules.reactivemongo.ReactiveMongoPlugin
import scala.io.Source
import play.api.libs.json.{JsArray, JsValue, Json}
import scala.collection.JavaConversions._
import play.modules.reactivemongo._
import play.modules.reactivemongo.PlayBsonImplicits._
import concurrent.ExecutionContext.Implicits.global
import play.api.libs.iteratee.Enumerator

class FixturesPlugin(app: Application) extends Plugin {
  override def enabled = filesOption.isDefined && !filesOption.get.isEmpty

  override def onStart() {
    super.onStart()
    filesOption.get.foreach { fileName =>
      loadFile(fileName)
    }
  }

  private def loadFile(fileName: String) {
    Logger.info(s"Loading Fixture file $fileName")
    val file = app.getFile(fileName)
    val fileContent = Source.fromFile(file).mkString
    val json = Json.parse(fileContent)
    json match {
      case array: JsArray => importToCollection(file.getName.replace(".json", ""), array)
      case _ => throw new RuntimeException("expected JSON array in $fileName")
    }
  }

  def importToCollection(collectionName: String, array: JsArray) {
    Logger.info(s"filling collection $collectionName")
    val collection = ReactiveMongoPlugin.db(app)(collectionName)
    //@TODO batch processing possible?
    array.value foreach { document =>
      collection.insert(document)
    }
  }

  private def filesOption = app.configuration.getStringList("fixtures.files")
}
