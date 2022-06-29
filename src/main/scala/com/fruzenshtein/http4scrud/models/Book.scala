package com.fruzenshtein.http4scrud.models

import com.fruzenshtein.http4scrud.models.Book.{Author, Title}

import scala.util.Random

case class Book(
    id: String =
      Random.alphanumeric.take(8).foldLeft("")((result, c) => result + c),
    title: Title,
    author: Author
)

object Book {
  type Title = String
  type Author = String
  type BookId = String
}
