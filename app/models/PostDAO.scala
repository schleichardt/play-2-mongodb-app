package models

import scala.concurrent.Future
import concurrent.ExecutionContext.Implicits.global

trait PostDAO {
  def obtain(): Future[Seq[Post]]
}

object PostDAO extends PostDAO {
  def obtain() = Future(Seq())
}