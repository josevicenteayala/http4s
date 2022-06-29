# http4s-CRUD

Sample from: http://fruzenshtein.com/http4s-another-crud-example/

## Versions

v1: first version, same as sample from web.

v2: changes
    - move objects to packages: repositories, models, routes
    - new Trait Repo[K, E, EK]

v3: simplify Trait Repo before continuing
    Discovered a bug:
    => when using PUT if id does not exists, the app
       add a new book. => FIXED

v4: new routes for Persons. Each person contains the following data:
        - id: String
        - name: String
        - countryOfBirth: String
    transform BookRoutes from object to class

v5: remove some dependencies/plugin's not needed:

    "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
    "org.specs2" %% "specs2-core" % Specs2Version % "test",
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),

    scalaVersion point-tx-injection = 2.12.10
    scalaVersion http4s-crud        = 2.12.11
    => almost the same version!

## To implement as in point-tx-ingestion

- method `awardStars` in `PointTxIngestionRoutes.scala` in path `src/main/scala/sbux/ucp/rewards/infrastructure/adapters/http/routes`
- it contains the following directives to implement in `http4s`
  - `gwsRequestIdAsCorrelationId`
  - `loggingStandard`
  - `metered`
  - `extractRequest`
    - use `@` in front of the path: `case req @ POST -> Root / "books" =>`
    - `req` contains all the information of the request

## Notes

- after using g8 I've removed all the files generated.
- also removed tests.
- I have to make some changes in Main object:

From:

```scala

    BlazeServerBuilder[IO]
      .bindHttp(9000, "0.0.0.0")
      .withHttpApp(httpRoutes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
```

To:

```scala
    import scala.concurrent.ExecutionContext

    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(9000, "0.0.0.0")
      .withHttpApp(httpRoutes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
```

### curl Books

#### GET all books

```
curl -X GET http://localhost:9000/books
```

#### POST add new book

```
curl -X POST http://localhost:9000/books \
  -H 'Content-Type: application/json' \
  -d '{
	"title": "Cosmos",
	"author": "Carl Sagan"
}'
```

Response:

```
{
    "id": "0ROonuZH"
}
```

#### GET book id:

```
curl -X GET http://localhost:9000/books/0ROonuZH
```

Response:

```
{
    "id": "0ROonuZH",
    "title": "Cosmos",
    "author": "Carl Sagan"
}
```

#### PUT update a book:

```
curl -X PUT http://localhost:9000/books/0ROonuZH \
  -H 'Content-Type: application/json' \
  -d '{
	"title": "Cosmos2",
	"author": "Carl Sagan2"
}'
```

#### DELETE a book

```
curl -X DELETE http://localhost:9000/books/0ROonuZH 
```

### curl Persons

Same as books.

### Compile and Run using sbt
* Stars sbt console

* Then compile the code

    sbt:http4s-crud-v6> compile
    [info] Compiling 11 Scala sources to /home/user/Projects/Starbucks/http4s-lab/http4s/target/scala-2.12/classes ...
    [success] Total time: 10 s, completed Jun 29, 2022 12:01:46 PM

* Then run the project

    sbt:http4s-crud-v6> run
    [info] running com.fruzenshtein.http4scrud.Main
    [io-compute-7] INFO  o.h.b.c.n.NIO1SocketServerGroup - Service bound to address /0:0:0:0:0:0:0:0:9000
    [io-compute-7] INFO  o.h.b.s.BlazeServerBuilder -
  _   _   _        _ _
| |_| |_| |_ _ __| | | ___
| ' \  _|  _| '_ \_  _(_-<
|_||_\__|\__| .__/ |_|/__/
|_|
[io-compute-7] INFO  o.h.b.s.BlazeServerBuilder - http4s v0.23.12 on blaze v0.23.12 started at http://[::]:9000/