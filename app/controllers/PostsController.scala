package controllers

import play.api.mvc.{MultipartFormData, AsyncResult, Action, Controller}
import models.{Post, PostDAO}
import play.api.data.Form
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger
import play.api.libs.Files.TemporaryFile

object PostsController extends Controller {
  val postDAO: PostDAO = PostDAO

  def save(id: String) = Action(parse.multipartFormData) {
    implicit request =>
      def errorPath(form: Form[Post]) = {
        Logger.info(form.errors.toString)
        BadRequest(views.html.editPost(form, None))
      }
      def happyPath(post: Post) = {

        val body: MultipartFormData[TemporaryFile] = request.body
        body.file("picture").map { a =>
          Logger.info("file has name " + a.filename)
          a.filename
        }

        AsyncResult {
          postDAO.updateBasics(post).map { _ => //TODO, does it really throw an error?
            Redirect(routes.PostsController.show(id)).flashing("success" -> s"Post ${post.title} successfully saved.")
          }
        }
      }
      Application.postForm.bindFromRequest().fold(errorPath, happyPath)
  }

  def show(id: String) = Action {implicit request =>
    AsyncResult {
      postDAO.byId(id).map( postOption =>
        postOption.map( post => Ok(views.html.editPost(Application.postForm.fill(post), Option(post)))).getOrElse(NotFound)
      )
    }
  }

  def delete(id: String) = Action {implicit request =>
    AsyncResult {
      postDAO.delete(id).map { _ =>
        Logger.info(s"deleting $id")
        Redirect(routes.Application.index).flashing("success" -> s"successfully deleted $id")
      }
    }
  }
}
