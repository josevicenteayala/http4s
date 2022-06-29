package com.fruzenshtein.http4scrud.routes

import cats.effect.IO
import com.fruzenshtein.http4scrud.models.Person
import com.fruzenshtein.http4scrud.repositories.Repo
import com.fruzenshtein.http4scrud.routes.data.PersonRequest
import io.circe.Json
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

import java.util.UUID

class PersonRoutes(personRepo: Repo[String, Person]) {

  private def errorBody(message: Message) =
    Json.obj(
      ("message", Json.fromString(message))
    )

  val routes: HttpRoutes[IO] = {
    val dsl = Http4sDsl[IO]
    import dsl._

    HttpRoutes.of[IO] {
      case _ @GET -> Root / "persons" =>
        personRepo.getList().flatMap(persons => Ok(persons))

      case req @ POST -> Root / "persons" =>
        req.decode[PersonRequest] { personRequest =>
          val person =
            Person(
              id = UUID.randomUUID().toString,
              name = personRequest.name,
              countryOfBirth = personRequest.countryOfBirth
            )
          personRepo.add(person) flatMap (id =>
            Created(Json.obj(("id", Json.fromString(id))))
          )
        }

      case _ @GET -> Root / "persons" / id =>
        personRepo.get(id) flatMap {
          case None       => NotFound()
          case Some(book) => Ok(book)
        }

      case req @ PUT -> Root / "persons" / id =>
        req.decode[PersonRequest] { personRequest =>
          val book =
            Person(
              id,
              name = personRequest.name,
              countryOfBirth = personRequest.countryOfBirth
            )
          personRepo.update(id, book) flatMap {
            case Left(message) => NotFound(errorBody(message))
            case Right(_)      => Ok()
          }
        }

      case _ @DELETE -> Root / "persons" / id =>
        personRepo.delete(id) flatMap {
          case Left(message) => NotFound(errorBody(message))
          case Right(_)      => Ok()
        }
    }
  }

}
