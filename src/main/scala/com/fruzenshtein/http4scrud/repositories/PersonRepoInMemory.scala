package com.fruzenshtein.http4scrud.repositories

import cats.effect.IO
import cats.implicits._
import com.fruzenshtein.http4scrud.models.Person
import io.circe.Json
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

import scala.collection.mutable

class PersonRepoInMemory extends Repo[String, Person] {

  import scala.collection.mutable.HashMap
  private val storage = mutable.HashMap[String, Person]().empty

  override def add(person: Person): IO[String] =
    IO {
      storage.put(person.id, person)
      person.id
    }

  override def get(id: String): IO[Option[Person]] =
    IO {
      storage
        .get(id)
    }

  override def delete(id: String): IO[Either[Message, Unit]] =
    for {
      removedPerson <- IO(storage.remove(id))
      result = removedPerson.toRight(s"Person with $id not found").void
    } yield result

  override def update(
      id: String,
      person: Person
  ): IO[Either[Message, Unit]] = {
    get(id).map {
      case Some(_) =>
        storage
          .put(id, person)
          .toRight(s"Person with $id not found")
          .void
      case None =>
        Left(s"Person with $id not found")
    }
  }

  override def getList(): IO[List[Person]] =
    IO {
      storage.map {
        case (_, person) => person
      }.toList
    }
}
