package search

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import models.Blog
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner._
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask
/**
 * Created by calvin-pc on 6/9/2015.
 */
@RunWith(classOf[JUnitRunner])
class SearchWorkerTest extends TestKit(ActorSystem("testSystem"))
with SpecificationLike
with ImplicitSender{
  "Worker actor" should {
    "return correct result" in {
      val testActorRef = TestActorRef[SearchWorker]
      val searchString = "ja"
      val allBlog:Seq[Blog] = Seq(
        Blog(1,"Sudarsa","Bolos kerja",""),
        Blog(2,"Hari ini...","hujan",""),
        Blog(3,"Bunga","melati",""),
        Blog(4,"besok","belajar scala",""),
        Blog(5,"kapan-kapan","main game","")
      )
      val shouldResult:Seq[Blog] = Seq(
        Blog(1,"Sudarsa","Bolos kerja",""),
        Blog(2,"Hari ini...","hujan",""),
        Blog(4,"besok","belajar scala","")
      )
      testActorRef ! Work(searchString,allBlog)
      val result = expectMsgClass(classOf[Result]).blogList
      result.forall(blog => shouldResult.contains(blog)) must_== true
      result.size must_== shouldResult.size
    }
  }
}