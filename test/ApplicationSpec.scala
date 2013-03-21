package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers.contentAsString
import org.specs2.execute.Pending
import models.{Post, PostDAO, MockPostDAO}
import play.api.mvc._
import org.openqa.selenium.WebDriver
import com.google.common.base.Predicate
import scala.collection.JavaConversions._
import concurrent.{Await, Awaitable}
import concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import play.api.test.Helpers.route
import play.api.test.Helpers.writeableOf_AnyContentAsFormUrlEncoded
import play.api.test.Helpers.status
import play.api.test.Helpers.CREATED
import play.api.test.Helpers.OK
import play.api.test.Helpers.POST
import play.api.test.Helpers.GET
import play.api.test.Helpers.writeableOf_AnyContentAsEmpty
import play.api.test.Helpers.SEE_OTHER
import play.api.test.Helpers.headers
import play.api.test.Helpers.cookies
import play.api.test.Helpers.redirectLocation
import org.joda.time.DateTime
import org.specs2.execute.Pending
import models.Post

object TestUtil {
  import TimeUnit._

  object ApplicationController extends Controller with controllers.Application {
      val postDAO = MockPostDAO
    }

  def createWait(browser: TestBrowser) = browser.await().atMost(1, SECONDS)

  implicit def function2Predicate(f: => Boolean) = new Predicate[WebDriver]{
    def apply(webDriver: WebDriver) = f
  }

  def await[T](awaitable: Awaitable[T]) = Await.result(awaitable, Duration("12 seconds"))
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

    "enable a user to change a post" in new WithApplication {
      val newTitle = "new title of 6"
      val newContent = "content update"
      val id = "element6"
      var request = { FakeRequest(POST, controllers.routes.PostsController.save(id).url).withFormUrlEncodedBody("id" -> id, "title" -> newTitle, "content" -> newContent)}
      val redirectResult = route(request).get
      status(redirectResult) must be equalTo(SEE_OTHER)
      cookies(redirectResult).get("PLAY_FLASH").map(_.value.contains("success")) must beSome(true)

      val result = route(FakeRequest(GET, redirectLocation(redirectResult).get)).get
      val changedPostOption = await(PostDAO.byId("element6"))
      changedPostOption must beSome
      val changedPost: Post = changedPostOption.get
      changedPost.title must be equalTo (newTitle)
      changedPost.content must be equalTo (newContent)
    }
  }
}

class NewPostPageSpec extends Specification {
  "The new post page" should {
    "enable a user to create a new post" in new WithBrowser {
      val pathFormPage = controllers.routes.Application.showCreatePostForm.url
      val url = s"http://localhost:$port%s".format(pathFormPage)
      browser.goTo(url)
      val postTemplate = Post("", "the new title", "the new content")
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


