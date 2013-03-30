package models

import concurrent.Future
import concurrent.ExecutionContext.Implicits.global

object MockPostDAO extends PostDAO {
  def obtain(limit: Int) = Future(Seq(Post("1", "Title 1 Mock", "Content 1"), Post("2", "Title 2 Mock", "Content 2")))
  def byId(id: String) = ???
  def updateBasics(post: Post) = ???
  def delete(id: String) = ???
  def insert(post: Post) = ???
}
