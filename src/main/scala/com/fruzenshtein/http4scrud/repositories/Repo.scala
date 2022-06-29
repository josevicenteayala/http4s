package com.fruzenshtein.http4scrud.repositories

import cats.effect.IO

/**
  *
  * @tparam K:  key of each entity
  * @tparam E:  entity
  */

trait Repo[K, E] {
  type Message = String

  def add(entity: E): IO[K]
  def get(key: K): IO[Option[E]]
  def delete(key: K): IO[Either[Message, Unit]]
  def update(key: K, entity: E): IO[Either[Message, Unit]]
  def getList(): IO[List[E]]
}
