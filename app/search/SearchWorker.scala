package search

import akka.actor.Actor
import akka.actor.Actor.Receive
import models.Blog

/**
 * Created by calvin-pc on 6/8/2015.
 */
class SearchWorker extends Actor{

  def receive = {
    case Work(searchString, blogList) =>
      sender ! Result(searchBlogList(searchString,blogList))
  }

  def searchBlogList(searchString: String, blogList:Seq[Blog]): Seq[Blog] = {
    return blogList.filter( blog => blog.title.contains(searchString)
      || blog.content.contains(searchString)  )
  }
}
