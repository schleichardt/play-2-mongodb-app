package test

import org.specs2.mutable._

import play.api.test._
import models.PostDAO
import play.api.test.Helpers._
import models.Post
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.StringBody

class PostUpdateSpec extends Specification {
  "The post page" should {
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
