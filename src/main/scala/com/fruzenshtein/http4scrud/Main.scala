package com.fruzenshtein.http4scrud

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.server.Router
import org.http4s.implicits._
import org.http4s.server.blaze._
import cats.implicits._
import com.fruzenshtein.http4scrud.models.{Book, Person}
import com.fruzenshtein.http4scrud.models.Book.BookId
import com.fruzenshtein.http4scrud.repositories.BookRepo.BookRepoInMemory
import com.fruzenshtein.http4scrud.repositories.{PersonRepoInMemory, Repo}
import com.fruzenshtein.http4scrud.routes.{
  AwardsRoutes,
  BookRoutes,
  PersonRoutes
}

object Main extends IOApp {

  private val bookRepo: Repo[BookId, Book] = new BookRepoInMemory
  private val bookRoutes = new BookRoutes(bookRepo)
  private val personRepo: Repo[String, Person] = new PersonRepoInMemory
  private val personRoutes = new PersonRoutes(personRepo)
  private val awardsRoutes = new AwardsRoutes

  val httpRoutes = Router(
    "/" -> bookRoutes.routesModifyHeaders,
    "/" -> personRoutes.routes,
    "/" -> awardsRoutes.routes
  ).orNotFound

  override def run(args: List[String]): IO[ExitCode] = {

    import scala.concurrent.ExecutionContext
    import org.http4s.blaze.server.BlazeServerBuilder

    BlazeServerBuilder[IO]
      .withExecutionContext(ExecutionContext.global)
      .bindHttp(9000, "0.0.0.0")
      .withHttpApp(httpRoutes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

}
