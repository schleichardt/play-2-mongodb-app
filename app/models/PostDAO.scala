package models

import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global

trait PostDAO {
  def obtain(): Future[Seq[Post]]
}

object PostDAO extends PostDAO {
  def obtain() = Future(Seq(Post("Title 1", "Content 1"), Post("Title 2", "Content 2")))
}