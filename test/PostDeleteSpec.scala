package test

import models.PostDAO
import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.test.WithBrowser
import test.TestUtil._
import scala.collection.JavaConversions._

class PostDeleteSpec extends Specification {
  "The post page" should {
    "enable a user to delete a post" in new WithBrowser {
      val id = "element6"
      def postExists = await(PostDAO.byId(id)).isDefined
      postExists === true
      val path = controllers.routes.PostsController.show(id).url
      browser.goTo(s"http://localhost:$port%s".format(path))
      browser.click("#delete-post")
      createWait(browser).until(browser.$(".alert").getTexts.mkString.
        contains("successfully deleted"))
      postExists === false
    }
  }
}
