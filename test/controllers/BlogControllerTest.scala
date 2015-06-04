package controllers


import dal.{CommentRepository, BlogRepository}
import models.{Comment, Blog}
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.inject.guice.GuiceApplicationBuilder
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.inject.bind
import play.api.test._
import play.api.test.Helpers._
/**
 * Created by calvin-pc on 6/4/2015.
 */

@RunWith(classOf[JUnitRunner])
class BlogControllerTest extends Specification with Mockito{

  val mockBRepo = mock[BlogRepository]
  val mockCRepo = mock[CommentRepository]
  val application = new GuiceApplicationBuilder()
    .bindings(bind[BlogRepository].to(mockBRepo))
    .bindings(bind[CommentRepository].to(mockCRepo))
    .build
  "BlogController" should {

    "show all blog" in new WithApplication(application) {
      mockBRepo.list() returns Future.apply(Seq(Blog(20,"Bulan Panjang","","28 agustus")))
      val home = route(FakeRequest(GET, "/home")).get
      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain(Await.result(mockBRepo.list(),1000 seconds).last.title)
    }

    "show add blog page" in new WithApplication(application){
      val page = route(FakeRequest(GET, "/add_blog")).get

      status(page) must equalTo(OK)
      contentType(page) must beSome.which(_ == "text/html")
      contentAsString(page) must contain("form")
    }

    "show spesific blog with all comment" in new WithApplication(application){
      mockBRepo.findByID(20) returns Future.apply(Blog(20,"Bulan Panjang","","28 agustus"))
      mockCRepo.list(20) returns
        Future.apply(Seq(
          Comment(1,20,"lala","muga","28 agustus"),
          Comment(3,20,"michi","huga","29 agustus")))
      val page = route(FakeRequest(GET, "/blog/20")).get

      status(page) must equalTo(OK)
      contentType(page) must beSome.which(_ == "text/html")
      contentAsString(page) must contain("Bulan Panjang")
      contentAsString(page) must contain("lala")
      contentAsString(page) must contain("michi")
    }

    "show update blog page" in new WithApplication(application){
      mockBRepo.findByID(20) returns Future.apply(Blog(20,"Bulan Panjang","","28 agustus"))
      val page = route(FakeRequest(GET, "/update/20")).get

      status(page) must equalTo(OK)
      contentType(page) must beSome.which(_ == "text/html")
      contentAsString(page) must contain("Bulan Panjang")
      contentAsString(page) must contain("form")
      contentAsString(page) must contain("update")
    }

    "save an add blog post" in new WithApplication(application){
      val request = FakeRequest(POST, "/blog")
        .withFormUrlEncodedBody(
        "title" -> "Baru",
        "content" -> "Coba-Coba"
        )
      val page = route(request).get

      Thread.sleep(500)
      there was one(mockBRepo).create("Baru","Coba-Coba")
    }

    "update blog post" in new WithApplication(application){
      mockBRepo.findByID(20) returns Future.apply(Blog(20,"Bulan Panjang","","28 agustus"))
      val request = FakeRequest(POST, "/update/20")
        .withFormUrlEncodedBody(
          "title" -> "Baru",
          "content" -> "Coba-Coba"
        )
      val page = route(request).get

      Thread.sleep(500)
      mockBRepo.updateBlogById(argThat(===(20)),===("Baru"),===("Coba-Coba"))
    }

    "delete blog post" in new WithApplication(application) {
      val request = FakeRequest(POST, "/delete/20")

      val page = route(request).get

      Thread.sleep(500)
      there was one(mockBRepo).deleteByID(20)
    }
  }
}