package search

import models.Blog

/**
 * Created by calvin-pc on 6/8/2015.
 */
sealed trait SearchMessage
case class Search(searchString: String,blogList: Seq[Blog]) extends SearchMessage
case class Work(searchString: String, blogList: Seq[Blog]) extends SearchMessage
case class Result(blogList: Seq[Blog]) extends SearchMessage
