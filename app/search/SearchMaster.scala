package search

import akka.actor.{Props, Actor, ActorRef}
import akka.routing.RoundRobinRouter
import dal.BlogRepository
import models.Blog
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by calvin-pc on 6/8/2015.
 */
class SearchMaster (nrOfWorker: Int, nrOfElement: Int, listener: ActorRef, blogRepo: BlogRepository)
  extends Actor{

  var result: Seq[Blog] = Seq()
  var nrOfResult: Int = 0
  var nrOfExpected: Int = 0
  val start: Long = System.currentTimeMillis()

  val workerRouter = context.actorOf(
    Props[SearchWorker].withRouter(RoundRobinRouter(nrOfWorker)), name = "workerRouter")

  def receive = {
    case Search(searchString) => {
      var list = Await.result(blogRepo.list(),1000 minutes)
      var iterate = list.length/nrOfElement
      for (i <- 0 until iterate) {
        workerRouter ! Work(searchString, list.take(nrOfElement))
        list = list.drop(nrOfElement)
      }
      nrOfExpected = iterate
    }

    case Result(blogList) => {
      result = result ++ blogList

      nrOfResult += 1

      if (nrOfResult == nrOfExpected) {
        listener ! Result(result)
        context.stop(self)
      }
    }
  }
}
