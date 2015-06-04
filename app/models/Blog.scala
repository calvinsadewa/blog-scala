package models

/**
 * Created by calvin-pc on 6/3/2015.
 */

import play.api.libs.json._

case class Blog(id: Long, title: String, content: String, date: String)
object Blog {
  implicit val blogFormat = Json.format[Blog]
}
