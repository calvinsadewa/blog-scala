package search

import akka.actor.{ActorRef, Actor}
import models.Blog

/**
 * Created by calvin-pc on 6/8/2015.
 */
class SearchListener extends Actor{
  var returnAddress:ActorRef = null
  def receive = {
    case Search(string:String) => returnAddress = sender()
    case Result(blogList) => returnAddress ! blogList
  }
}
