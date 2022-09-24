package com.teamextn.http

import cats.Parallel
import cats.effect.{Async, ExitCode}
import cats.implicits.toFunctorOps
import com.teamextn.AppConfig.HttpConfig
import com.teamextn.http.dsl.{CrawlerDsl, HealthCheckDsl}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.syntax.all._
import org.http4s.HttpRoutes
import org.typelevel.log4cats.Logger

final case class HttpServer[F[_]: Async](routes: HttpRoutes[F], httpConfig: HttpConfig) {
  def start: F[ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(httpConfig.port, httpConfig.host)
      .withHttpApp(routes.orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}

object HttpServer {

  def apply[F[_]: Async: Logger: Parallel](httpConfig: HttpConfig, crawlerClient: CrawlerClient[F]): HttpServer[F] = {

    val crawlerDsl     = new CrawlerDsl[F](crawlerClient)
    val healthCheckDsl = new HealthCheckDsl[F]

    val routes = Router[F](
      "isAlive"    -> healthCheckDsl.routes,
      "/api/crawl" -> crawlerDsl.routes
    )

    new HttpServer[F](routes, httpConfig)
  }
}
