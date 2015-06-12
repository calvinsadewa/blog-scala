package dal

import java.sql.Date
import javax.inject.{Inject, Singleton}

import models.{Comment, Blog}
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{Future, ExecutionContext}

/**
 * A repsotiory for blog.
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class BlogRepository @Inject() (dbConfigProvider: DatabaseConfigProvider, comRepo: CommentRepository)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  /**
   * Here we define the table. It will have blog
   */
  private class BlogTable(tag: Tag) extends Table[Blog](tag, "blog") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The title column */
    def title = column[String]("title")

    /** The content column */
    def content = column[String]("content")

    /** The date column */
    def date = column[String]("date")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Blog object.
     *
     */
    def * = (id, title, content, date) <> ((Blog.apply _).tupled, Blog.unapply)
  }

  /**
   * The starting point for all queries on the blog table.
   */
  private val blogs = TableQuery[BlogTable]

  /**
   * Create a blog with the given title and content.
   *
   * This is an asynchronous operation, it will return a future of the created blog, which can be used to obtain the
   * id for that person.
   */
  def create(title: String, content: String): Future[Blog] = db.run {
    // We create a protection of just the title and content columns, since we're not inserting a value for the id column
    (blogs.map(p => (p.title, p.content))
      // Now define it to return the id, because we want to know what id was generated for the blog
      returning blogs.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((titleContent, id) => Blog(id, titleContent._1, titleContent._2, ""))
      // And finally, insert the person into the database
      ) += (title, content)
  }

  /**
   * List all the blog in the database, sorted by date descending, asynchronus.
   */
  def list(): Future[Seq[Blog]] = db.run {
    blogs.sortBy(_.date.desc.nullsFirst).result
  }

  /**
   * Delete the blog in the database with given id, sychronous.
   */
  def deleteByID(id : Long) :Future[_] = db.run {
    comRepo.deleteByIdBlog(id)
    blogs.filter(_.id === id).delete
  }

  /**
   * Find the blog in the database with given id, asynchronus.
   */
  def findByID(id : Long) :Future[Blog] = db.run {
    blogs.filter(_.id === id).result.head
  }

  /**
   * update blog with given id in database, asynchronus.
   */
  def updateBlogById(id: Long, title: String, content: String) : Future[_] = db.run {
    val q = for { b <- blogs if b.id === id } yield (b.title,b.content)
    q.update((title,content))
  }
}