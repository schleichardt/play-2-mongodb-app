package test

import models.{PostDAO, Post}
import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.test.WithBrowser
import TestUtil._
import scala.collection.JavaConversions._

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
