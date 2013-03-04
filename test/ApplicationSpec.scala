package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers.contentAsString
import org.specs2.execute.Pending
import models.{Post, PostDAO, MockPostDAO}
import play.api.mvc.{Controller, AnyContent, Action, Result}
import org.openqa.selenium.WebDriver
import com.google.common.base.Predicate
import scala.collection.JavaConversions._
import concurrent.{Await, Awaitable}
import concurrent.duration.Duration
import java.util.concurrent.TimeUnit

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends Specification {
  "Application" should {
    "enable a user to registrate" in {
      Pending
    }

    "enable a user to login" in {
      Pending
    }

    "enable a user to logout" in {
      Pending
    }
  }
}

object TestUtil {
  import TimeUnit._

  object ApplicationController extends Controller with controllers.Application {
      val postDAO = MockPostDAO
    }

  def createWait(browser: TestBrowser) = browser.await().atMost(1, SECONDS)

  implicit def function2Predicate(f: => Boolean) = new Predicate[WebDriver]{
    def apply(webDriver: WebDriver) = f
  }

  def await[T](awaitable: Awaitable[T]) = Await.result(awaitable, Duration("10 seconds"))
}

import TestUtil._

class StartPageSpec extends Specification {
  "The start page" should {
    "show the teasers of the 10 most recent blog posts" in new WithApplication {
      val result: Result = controllers.Application.index()(FakeRequest())
      val content = contentAsString(result)
      for (i <- 5 to 14) {
        content must contain(s"title $i")
      }
      content must not contain("title 4")
    }

    "makes older posts paginated available" in {
      Pending
    }
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
      Pending
    }

    "enable a user to make a comment" in {
      Pending
    }
  }
}

class NewPostPageSpec extends Specification {
  "The new post page" should {
    "enable a user to create a new post" in new WithBrowser {
      val pathFormPage = controllers.routes.Application.showCreatePostForm.url
      val url = s"http://localhost:$port%s".format(pathFormPage)
      browser.goTo(url)
      val postTemplate = Post("the new title", "the new content")
      browser.fill("#edit-post-title").`with`(postTemplate.title)
      browser.fill("#edit-content").`with`(postTemplate.content)
      browser.$("#edit-post-submit").click()
      createWait(browser).until(browser.$(".alert").getTexts.mkString.contains("successfully created"))
      val persistedPost = await(PostDAO.obtain(1)).head
      persistedPost.title must beEqualTo(postTemplate.title)
      persistedPost.content must beEqualTo(postTemplate.content)
    }.pendingUntilFixed("in progress")
  }
}


