package controllers

import javax.inject.Inject

import akka.actor.{Props, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import dal.{CommentRepository, BlogRepository}
import models.Blog
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent._
import scala.concurrent.duration._
import scala.text

import search._
/**
 * Created by calvin-pc on 6/3/2015.
 */
class BlogController @Inject() (blogRepo:BlogRepository, comRepo:CommentRepository, val messagesApi: MessagesApi)
                               (implicit ec: ExecutionContext) extends Controller with I18nSupport{

  /**
   * mapping for blog form
   */
  val blogForm: Form[CreateBlogForm] = Form {
    mapping(
      "title" -> nonEmptyText,
      "content" -> nonEmptyText
    )(CreateBlogForm.apply)(CreateBlogForm.unapply)
  }

  /**
   * mapping for comment form
   */
  val commentForm: Form[CommentForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "content" -> nonEmptyText
    )(CommentForm.apply)(CommentForm.unapply)
  }

  /**
   * mapping for search form
   */
  val searchForm: Form[SearchForm] = Form {
    mapping(
      "search" -> nonEmptyText
    )(SearchForm.apply)(SearchForm.unapply)
  }

  /**
   * The index action.
   */
  def index = Action {
    var result = blogRepo.list()
    Ok(views.html.home(Await.result(result, 10000 seconds)))
  }

  /**
   * Add blog page action
   */
  def addPage = Action {
    Ok(views.html.add_blog(blogForm))
  }

  /**
   * The add blog to database api action.
   *
   */
  def addBlog = Action.async { implicit request =>
    // Bind the form first, then fold the result, passing a function to handle errors, and a function to handle succes.
    blogForm.bindFromRequest.fold(
      // The error function. We return the index page with the error form, which will render the errors.
      // We also wrap the result in a successful future, since this action is synchronous, but we're required to return
      // a future because the person creation function returns a future.
      errorForm => {
        Future.successful(Ok(views.html.add_blog(errorForm)))
      },
      // There were no errors in the from, so create the blog.
      blog => {
        blogRepo.create(blog.title, blog.content).map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.BlogController.index)
        }
      }
    )
  }

  /**
   * A REST endpoint that gets all the people as JSON.
   */
  def getBlogs = Action.async {
    blogRepo.list().map { blogs =>
      Ok(Json.toJson(blogs))
    }
  }

  /**
   * view spesific blog action
   */
  def getBlog(id: String) = Action.async {
    blogRepo.findByID(id.toInt).map( blog =>
      Ok(views.html.blog(blog,commentForm,Await.result(comRepo.list(id.toInt),100000 seconds)))
    )
  }

  /**
   * A REST endpoint that delete a blog with spesific id
   */
  def deleteBlog(id: String) = Action {
    blogRepo.deleteByID(id.toInt)
    Redirect(routes.BlogController.index)
  }

  /**
   * a update blog page action
   */
  def updatePage(id: String) = Action.async {
    blogRepo.findByID(id.toInt).map( blog =>
    {
      val filledForm = blogForm.fill(CreateBlogForm(blog.title,blog.content))
      Ok(views.html.update(filledForm,id));
    }
    )
  }

  /**
   * REST endpoint that update blog with spesific id
   */
  def updateBlog(id: String) = Action.async { implicit request =>
    blogForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.update(errorForm,id)))
      },
      blog => {
        Future.successful( {
          blogRepo.updateBlogById(id.toInt,blog.title, blog.content)
          Redirect(routes.BlogController.index)
        })
      }
    )
  }

  /**
   * REST endpoint that add comment with spesific blog id
   */
  def addComment(id: String) = Action.async { implicit request =>
    commentForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.blog(Await.result(blogRepo.findByID(id.toInt),100000 seconds),
          errorForm,Await.result(comRepo.list(id.toInt),100000 seconds))))
      },
      comment => {
        Future.successful( {
          comRepo.create(id.toInt,comment.name, comment.content)
          Redirect(routes.BlogController.getBlog(id))
        })
      }
    )
  }

  /**
   * REST endpoint that search blog
   */
  def searchBlog() = Action.async { implicit request =>
    searchForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.search(errorForm)))
      },
      search => {
        implicit val timeout = Timeout(5 seconds)
        var system = ActorSystem()
        var listener = system.actorOf(Props(new SearchListener()))
        var master = system.actorOf(Props(new SearchMaster(1,1,listener,blogRepo)))
        master ! Search(search.search)
        ask(listener,Search("")).map(blogList =>
          Ok(views.html.home(blogList.asInstanceOf[Seq[Blog]]))
        )
      }
    )
  }

  /**
   * Search action
   */
  def search = Action {
    Ok(views.html.search(searchForm))
  }
}
case class CreateBlogForm(title: String, content: String)
case class CommentForm(name: String, content: String)
case class SearchForm(search: String)