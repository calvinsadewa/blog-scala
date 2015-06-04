package models

import play.api.libs.json.Json

/**
 * Created by calvin-pc on 6/3/2015.
 */
case class Comment(id: Long, id_blog: Long, name: String, content: String, date: String)
object Comment {
  implicit val commentFormat = Json.format[Blog]
}
