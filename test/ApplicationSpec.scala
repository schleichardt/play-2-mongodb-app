package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers.contentAsString
import models.{PostDAO, MockPostDAO}
import play.api.mvc._
import org.openqa.selenium.WebDriver
import com.google.common.base.Predicate
import scala.collection.JavaConversions._
import java.util.concurrent.TimeUnit
import play.api.test.Helpers._
import org.joda.time.DateTime
import scala._
import org.specs2.execute.Pending
import models.Post
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.StringBody

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

    "enable a user to change a post" in new WithServer {
      val newTitle = "new title of 6"
      val newContent = "content update"
      val id = "element6"
      val url = "http://localhost:%d%s".format(port, controllers.routes.PostsController.save(id).url)
      val post = new HttpPost(url)
      val reqEntity = new MultipartEntity()
      reqEntity.addPart("id", new StringBody(id))
      reqEntity.addPart("title", new StringBody(newTitle))
      reqEntity.addPart("content", new StringBody(newContent))
      post.setEntity(reqEntity)
      val response = new DefaultHttpClient().execute(post)
      response.getStatusLine().getStatusCode() must beEqualTo(200)

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


