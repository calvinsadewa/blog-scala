package search

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import models.Blog
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner._

/**
 * Created by calvin-pc on 6/9/2015.
 */
@RunWith(classOf[JUnitRunner])
class SearchMasterTest extends TestKit(ActorSystem("testSystem"))
with SpecificationLike
with ImplicitSender{
  "Master actor" should {
    "return correct result" in {
      val testActorRef = TestActorRef(new SearchMaster(1,1))
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
      testActorRef ! Search(searchString,allBlog)
      val result = expectMsgClass(classOf[Result]).blogList
      result.forall(blog => shouldResult.contains(blog)) must_== true
      result.size must_== shouldResult.size
    }
  }
}