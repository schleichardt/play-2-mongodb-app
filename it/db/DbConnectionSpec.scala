package info.schleichardt.ic2.db

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import info.schleichardt.ic2.db.DbTestTools._

class DbConnectionSpec extends Specification {

  "The application" can {
    "start with salat plugin" in {
      implicit val app = FakeApplication()
      running(app) {
        salatPlugin.enabled === true
      }
    }

    "connect to the MongoDB" in {
      implicit val app = FakeApplication()
      running(app) {
        salatPlugin.db().stats.ok === true
      }
    }
  }
}