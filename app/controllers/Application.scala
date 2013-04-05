package controllers

import play.api._
import play.api.mvc._
import models.{Comment, Post, PostDAO}
import concurrent.ExecutionContext.Implicits.global
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import templates.Html
import springdata.{Person, ApplicationConfig, SomeClient}
import org.springframework.context.annotation.AnnotationConfigApplicationContext

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

  def springdata = Action { implicit request =>
    val context = new AnnotationConfigApplicationContext(classOf[ApplicationConfig])
    context.scan("springdata", "controllers")
    val someClientBean = context.getBean(classOf[SomeClient])

    someClientBean.getRepository.save(new Person("Steve"))
    Ok("found " + someClientBean.getRepository.findByName("Steve").get(0))
  }

  def showCreatePostForm = Action { implicit request =>
    Ok(views.html.editPost(postForm, None))
  }

  def addPost = Action { implicit request =>
    val successPath: (Post) => AsyncResult = post => {
      Async {
        postDAO.insert(post).map { _ =>
          Redirect(routes.Application.index()).flashing("success" -> s"Post ${post.title} successfully created.")
        }
      }
    }
    val hasErrorsPath: (Form[Post]) => SimpleResult[Html] = formWithErrors =>
      BadRequest(views.html.editPost(formWithErrors, None))
    postForm.bindFromRequest.fold(hasErrorsPath, successPath)
  }
}

object Application extends Controller with Application {
  val postDAO = PostDAO
}