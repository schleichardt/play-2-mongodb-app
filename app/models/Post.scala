package models

import org.joda.time.DateTime

case class Comment(author: String, email: String, content: String, publishedAt: DateTime)

case class Post(id: String, title: String, content: String, comments: List[Comment] = Nil)