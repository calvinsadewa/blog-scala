package dal

import javax.inject.{Inject, Singleton}

import models.{Comment, Blog}
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * A repsotiory for comment.
  * @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class CommentRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
   // We want the JdbcProfile for this provider
   private val dbConfig = dbConfigProvider.get[JdbcProfile]

   // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
   // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
   import dbConfig._
   import driver.api._

   /**
    * Here we define the table. It will have a list of comment
    */
   private class CommentTable(tag: Tag) extends Table[Comment](tag, "comment") {

     /** The ID column, which is the primary key, and auto incremented */
     def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

     /** The ID_Blogcolumn, which is the refrence key*/
     def id_blog = column[Long]("id_blog")

     /** The name column */
     def name = column[String]("name")

     /** The content column */
     def content = column[String]("content")

     /** The date column */
     def date = column[String]("date")

     /**
      * This is the tables default "projection".
      *
      * It defines how the columns are converted to and from the Comment object.
      */
     def * = (id, id_blog, name, content, date) <> ((Comment.apply _).tupled, Comment.unapply)
   }

   /**
    * The starting point for all queries on the comment table.
    */
   private val comments = TableQuery[CommentTable]

   /**
    * Create a comment with the given blog id, name, and content, asynchronous
    */
   def create(id_blog: Long, name: String, content: String): Future[Comment] = db.run {
     (comments.map(p => (p.id_blog, p.name, p.content))
       returning comments.map(_.id)
       into ((titleContent, id) => Comment(id, titleContent._1, titleContent._2, titleContent._3, ""))
       ) += (id_blog, name, content)
   }

   /**
    * List comment by blog, asynchronous.
    */
   def list(id_blog: Long): Future[Seq[Comment]] = db.run {
     comments.filter(_.id_blog === id_blog).sortBy(_.date.asc.nullsFirst).result
   }

  /**
   * Delete all comment associated with blog id, synchronous
   */
   def deleteByIdBlog(id_blog : Long) :Unit = db.run {
     comments.filter(_.id_blog === id_blog).delete
   }
 }