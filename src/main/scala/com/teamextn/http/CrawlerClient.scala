package com.teamextn.http

import cats.effect.kernel.Concurrent
import cats.implicits.{catsSyntaxApplicativeError, toFunctorOps}
import com.teamextn.http.Models.CrawlResponse
import org.http4s.client.Client

import java.net.URL

trait CrawlerClient[F[_]] {
  def get(url: URL): F[CrawlResponse]
}

object CrawlerClient {
  def apply[F[_]: Concurrent](
      httpClient: Client[F]
  ): CrawlerClient[F] =
    (url: URL) =>
      httpClient
        .expect[String](url.toString)
        .map[CrawlResponse](CrawlResponse.Success(_, url))
        .handleError(err => CrawlResponse.Error(err.getLocalizedMessage, url))
}
