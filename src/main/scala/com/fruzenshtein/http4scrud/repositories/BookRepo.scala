package com.fruzenshtein.http4scrud.repositories

import cats.effect.IO
import cats.implicits._
import com.fruzenshtein.http4scrud.models.Book
import com.fruzenshtein.http4scrud.models.Book.BookId

import scala.collection.mutable

object BookRepo {

  class BookRepoInMemory extends Repo[BookId, Book] {

    import scala.collection.mutable.HashMap
    private val storage = mutable.HashMap[BookId, Book]().empty

    override def add(book: Book): IO[BookId] =
      IO {
        storage.put(book.id, book)
        book.id
      }

    override def get(id: BookId): IO[Option[Book]] =
      IO {
        storage
          .get(id)
      }

    override def delete(id: BookId): IO[Either[Message, Unit]] =
      for {
        removedBook <- IO(storage.remove(id))
        result = removedBook.toRight(s"Book with $id not found").void
      } yield result

    override def update(
        id: BookId,
        book: Book
    ): IO[Either[Message, Unit]] = {

      get(id).map {
        case Some(_) =>
          storage
            .put(id, book)
            .toRight(s"Book with $id not found")
            .void
        case None =>
          Left(s"Book with $id not found")
      }

// This code contains a bug: when the id doesn't exists
// the book is added in the repo anyway.
//      for {
//        bookOpt <- get(id)
//        _ <- IO(bookOpt.toRight(s"Book with $id not found").void)
//        updatedBook =
//          storage
//            .put(id, book)
//            .toRight(s"Book with $id not found")
//            .void
//      } yield updatedBook
    }

    override def getList(): IO[List[Book]] =
      IO {
        storage.map {
          case (_, book) => book
        }.toList
      }
  }

}
