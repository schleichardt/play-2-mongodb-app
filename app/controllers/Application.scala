package controllers

import play.api._
import play.api.mvc._
import models.{Comment, Post, PostDAO}
import concurrent.ExecutionContext.Implicits.global
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

trait Application {
  this: Controller =>

  val postDAO: PostDAO

  val postForm = Form(
    mapping(
      "id" -> text,
      "title" -> text.verifying(nonEmpty, minLength(5)),
      "content" -> text.verifying(nonEmpty, minLength(5)),
      "comments" -> ignored(List[Comment]())
    )(Post.apply)(Post.unapply)
  )

  def index = Action { implicit request =>
    Async {
      postDAO.obtain().map(posts => Ok(views.html.index(posts)))
    }
  }

  def showCreatePostForm = Action { implicit request =>
    Ok(views.html.editPost(postForm, None))
  }

  def addPost = Action { implicit request =>
    val filledForm = postForm.bindFromRequest
    filledForm.fold(
      formWithErrors =>
        BadRequest(views.html.editPost(formWithErrors, None)),
      value =>
        Redirect(routes.Application.index()).flashing("success" -> "Post successfully created.") //TODO go to edit page of post
    )
  }
}

object Application extends Controller with Application {
  val postDAO = PostDAO
}