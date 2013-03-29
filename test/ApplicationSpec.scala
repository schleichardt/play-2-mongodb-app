package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers.contentAsString
import models.{PostDAO, MockPostDAO}
import play.api.mvc._
import org.openqa.selenium.WebDriver
import com.google.common.base.Predicate
import java.util.concurrent.TimeUnit
import play.api.test.Helpers._
import org.joda.time.DateTime
import scala._
import org.specs2.execute.Pending

object TestUtil {
  import TimeUnit._

  object ApplicationController extends Controller with controllers.Application {
      val postDAO = MockPostDAO
    }

  def createWait(browser: TestBrowser) = browser.await().atMost(1, SECONDS)

  implicit def function2Predicate(f: => Boolean) = new Predicate[WebDriver]{
    def apply(webDriver: WebDriver) = f
  }
}

class TagPageSpec extends Specification {
  "The tag page" should {
    "show posts according a tag" in {
      Pending
    }
  }
}

class PostPageSpec extends Specification {
  "The post page" should {
    "show a full post with it's comments" in {
      "using a deserializer" in new WithApplication {
        val post = await(PostDAO.byId("element14")).get
        val firstComment = post.comments(0)
        firstComment.author === "Bernd"
        firstComment.content === "good page"
        val secondComment = post.comments(1)
        secondComment.author === "Frank Stallone"
        secondComment.publishedAt === new DateTime(1353387915242L)
      }

      "in the HTML source code" in new WithApplication {
        var request = { FakeRequest(GET, controllers.routes.PostsController.show("element14").url)}
        val result = route(request).get
        status(result) must be equalTo(OK)
        val content = contentAsString(result)
        content must contain("Frank Stallone")
      }
    }

    "enable a user to make a comment" in {
      Pending
    }
  }
}




