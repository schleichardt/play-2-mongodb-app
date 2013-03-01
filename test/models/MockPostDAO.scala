package models

import concurrent.Future
import concurrent.ExecutionContext.Implicits.global

object MockPostDAO extends PostDAO {
  def obtain() = Future(Seq(Post("Title 1 Mock", "Content 1"), Post("Title 2 Mock", "Content 2")))
}
