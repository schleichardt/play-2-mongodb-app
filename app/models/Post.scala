package models

import org.joda.time.DateTime

case class Comment(author: String, emain: String, content: String, published: DateTime)

case class Post(id: String, title: String, content: String, comments: List[Comment] = Nil)