package controllers

import play.api._
import play.api.mvc._
import models.PostDAO
import concurrent.ExecutionContext.Implicits.global

trait Application {
  this: Controller =>

  val postDAO: PostDAO

  def index = Action {
    Async {
      postDAO.obtain().map(posts => Ok(views.html.index(posts)))
    }
  }
}

object Application extends Controller with Application {
  val postDAO = PostDAO
}