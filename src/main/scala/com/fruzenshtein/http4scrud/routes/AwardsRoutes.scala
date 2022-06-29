package com.fruzenshtein.http4scrud.routes

import cats.data.Kleisli
import cats.effect.IO
import org.http4s.{Header, Headers, HttpRoutes, Request, Response, Status}
import org.http4s.dsl.Http4sDsl
import org.http4s.util.CaseInsensitiveString
import org.typelevel.ci.CIString

class AwardsRoutes {

  private def replaceHeaders(request: Request[IO]): Headers = {
    val searchHeader = "CustomHeaders"
    val newHeader = "NewHeaders"

    request.headers.get(CIString(searchHeader)) match {
      case Some(headerFound) =>
        //request.headers.headers.filterNot(_.name == CIString(searchHeader))
        //request.headers.put(Header(newHeader, headerFound.value))
        request.headers.put(
          Header.Raw(CIString(newHeader), "this is a new value")
        )
      case None =>
        request.headers
    }
  }

  def myMiddle(service: HttpRoutes[IO], header: Header.Raw): HttpRoutes[IO] =
    Kleisli { (req: Request[IO]) =>
      service(req).map {
        case Status.Successful(resp) =>
          resp.putHeaders(header)
        case resp =>
          resp
      }
    }

  val routes2: HttpRoutes[IO] = {
    val dsl = Http4sDsl[IO]
    import dsl._

    HttpRoutes.of[IO] {
      case _ @GET -> Root / "awards" =>
        Ok("GET Awards !!!")

      case req @ POST -> Root / "awards" =>
        // replace one header for another
        val newHeaders = replaceHeaders(req)

        IO {
          Response[IO](Status.Ok)
            .withHeaders(newHeaders)
            .withEntity[String]("something")
        }

    }
  }

  val routes: HttpRoutes[IO] =
    myMiddle(
      routes2,
      Header.Raw(CIString("SomeKey"), "SomeValue")
    );
}
