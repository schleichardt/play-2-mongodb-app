package controllers

import play.api.mvc.{AsyncResult, Action, Controller}
import models.PostDAO
import reactivemongo.bson.{BSONString, BSONDateTime, BSONDocument}
import org.joda.time.DateTime
import play.api.Logger

object PostsController extends Controller {
  val postDAO: PostDAO = PostDAO

  def save(id: String) = Action {
    implicit request =>
      Application.postForm.bindFromRequest().fold(
        form => BadRequest(views.html.editPost(form, None)),
        post => AsyncResult {
          import reactivemongo.bson.handlers.DefaultBSONHandlers.DefaultBSONDocumentWriter
          import scala.concurrent.ExecutionContext.Implicits.global
          val modifier = BSONDocument("$set" -> BSONDocument("title" -> BSONString(post.title), "content" -> BSONString(post.content)))
          //TODO code to PostDAO
          val query = BSONDocument("_id" -> BSONString(id))
          PostDAO.collection.update(query, modifier).map { _ =>
            Redirect(routes.PostsController.show(id))
          }
        }
      )
  }

  def show(id: String) = Action {implicit request =>
    AsyncResult {
      import scala.concurrent.ExecutionContext.Implicits.global
      postDAO.byId(id).map( postOption =>
        postOption.map( post => Ok(views.html.editPost(Application.postForm.fill(post), Option(post)))).getOrElse(NotFound)
      )
    }
  }
}
