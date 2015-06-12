package dal

import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.WithApplication

import scala.concurrent._
import scala.concurrent.duration._

/**
 * Created by calvin-pc on 6/4/2015.
 */
@RunWith(classOf[JUnitRunner])
class CommentRepositoryTest extends Specification with Mockito{
  "CommentRepository" should {

    "able to add, find, update and delete" in new WithApplication() {
      val repo = play.api.Application.instanceCache[CommentRepository](implicitly).apply(play.api.Play.current)
      repo.create(1,"Bunga","Baru")
      Thread.sleep(500)
      var result = Await.result(repo.list(1),10 seconds)
      result.exists(comm => comm.id_blog == 1
        && comm.name.eq("Bunga")
        && comm.content.eq("Baru")) mustEqual(true)
      repo.deleteByIdBlog(1)
      Thread.sleep(500)
      result = Await.result(repo.list(1),10 seconds)
      result.exists(comm => comm.id_blog == 1) mustEqual(false)
    }
  }
}
