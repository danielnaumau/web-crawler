package com.teamextn.http

import cats.effect.kernel.Concurrent
import cats.implicits.{catsSyntaxApplicativeError, toFunctorOps}
import com.teamextn.http.Models.CrawlResponse
import org.http4s.client.Client
import org.http4s.Uri

trait CrawlerClient[F[_]] {
  def get(uri: Uri): F[CrawlResponse]
}

object CrawlerClient {
  def apply[F[_]: Concurrent](
      httpClient: Client[F],
  ): CrawlerClient[F] =
    (uri: Uri) =>
      httpClient
        .expect[String](uri)
        .map[CrawlResponse](CrawlResponse.Success(_, uri))
        .handleError(err => CrawlResponse.Error(err.getLocalizedMessage, uri))
}
