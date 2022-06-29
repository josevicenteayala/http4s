package com.fruzenshtein.http4scrud.routes.data

import com.fruzenshtein.http4scrud.models.Book.{Author, Title}

case class BookRequest(title: Title, author: Author)

case class PersonRequest(name: String, countryOfBirth: String)
