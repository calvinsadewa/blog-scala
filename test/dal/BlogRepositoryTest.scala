package dal

import java.util.Timer
import javax.inject.Inject

import models.{Comment, Blog}
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test._
import play.api.test.Helpers._
import scala.concurrent._
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scalaz.Inject

/**
 * Created by calvin-pc on 6/4/2015.
 */
@RunWith(classOf[JUnitRunner])
class BlogControllerTest extends Specification with Mockito{
  "BlogRepository" should {

    "able to add, find, update and delete" in new WithApplication() {
      val repo = play.api.Application.instanceCache[BlogRepository](implicitly).apply(play.api.Play.current)
      repo.create("Bunga","Baru")
      Thread.sleep(500)
      var result = Await.result(repo.list(),10000 seconds)
      result.exists(blog => blog.title.eq("Bunga") && blog.content.eq("Baru")) mustEqual(true)
      val id = result.filter(blog => blog.title.eq("Bunga") && blog.content.eq("Baru")).head.id
      repo.updateBlogById(id,"Gajah","Terbang")
      Thread.sleep(500)
      result = Await.result(repo.list(),10000 seconds)
      result.exists(blog => blog.title.eq("Gajah")
        && blog.content.eq("Terbang")
        && blog.id == id) mustEqual(true)
      repo.deleteByID(id)
      Thread.sleep(500)
      result = Await.result(repo.list(),10000 seconds)
      result.exists(blog => blog.id == id) mustEqual(false)
    }
  }
}