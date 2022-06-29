package com.fruzenshtein.http4scrud.routes

import cats.data.Kleisli
import cats.effect.IO
import com.fruzenshtein.http4scrud.models.Book
import com.fruzenshtein.http4scrud.models.Book.BookId
import com.fruzenshtein.http4scrud.repositories.Repo
import com.fruzenshtein.http4scrud.routes.data.BookRequest
import io.circe.Json
import io.circe.generic.auto._
import org.http4s.{Header, HttpRoutes, Request, Status}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.typelevel.ci.CIString

class BookRoutes(bookRepo: Repo[BookId, Book]) {

  private def errorBody(message: Message) =
    Json.obj(
      ("message", Json.fromString(message))
    )

  val routes: HttpRoutes[IO] = {

    val dsl = Http4sDsl[IO]
    import dsl._

    HttpRoutes.of[IO] {
      case _ @GET -> Root / "books" =>
        bookRepo.getList().flatMap(books => Ok(books))

      case req @ POST -> Root / "books" =>
        req.decode[BookRequest] { bookRequest =>
          val book =
            Book(title = bookRequest.title, author = bookRequest.author)
          bookRepo.add(book) flatMap (id =>
            Created(Json.obj(("id", Json.fromString(id))))
          )
        }

      case _ @GET -> Root / "books" / id =>
        bookRepo.get(id) flatMap {
          case None       => NotFound()
          case Some(book) => Ok(book)
        }

      case req @ PUT -> Root / "books" / id =>
        req.decode[BookRequest] { bookRequest =>
          val book =
            Book(id, title = bookRequest.title, author = bookRequest.author)
          bookRepo.update(id, book) flatMap {
            case Left(message) => NotFound(errorBody(message))
            case Right(_)      => Ok()
          }
        }

      case _ @DELETE -> Root / "books" / id =>
        bookRepo.delete(id) flatMap {
          case Left(message) => NotFound(errorBody(message))
          case Right(_)      => Ok()
        }
    }
  }

  def routesModifyHeaders: HttpRoutes[IO] =
    myMiddle(
      routes,
      Header.Raw(CIString("SomeHeaderAdded"), "New Header has been added")
    )

  def myMiddle(service: HttpRoutes[IO], header: Header.Raw): HttpRoutes[IO] =
    Kleisli { (req: Request[IO]) =>
      service(req).map {
        case Status.Successful(resp) =>
          resp.putHeaders(header)
        case resp =>
          resp
      }
    }

}
