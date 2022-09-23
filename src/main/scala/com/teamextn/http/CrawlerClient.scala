package com.teamextn.http

import cats.effect.kernel.Concurrent
import cats.implicits.toFunctorOps
import com.teamextn.http.Models.CrawlResponse
import org.http4s.client.Client
import org.http4s.{EntityBody, Response, Uri}

trait CrawlerClient[F[_]] {
  def get(uri: Uri): F[CrawlResponse]
}

object CrawlerClient {

  def apply[F[_]: Concurrent](
      httpClient: Client[F],
  ): CrawlerClient[F] = new CrawlerClient[F] {

    override def get(uri: Uri): F[CrawlResponse] = {
      httpClient.get[CrawlResponse](uri)(processResponse(_, uri))
    }

    private def processResponse(response: Response[F], uri: Uri): F[CrawlResponse] =
      if (response.status.isSuccess)
        parseBody(response.body).map(CrawlResponse.Success(_, uri))
      else
        Concurrent[F].pure(CrawlResponse.Error(response.body.toString(), uri, response.status.code))

    private def parseBody(entityBody: EntityBody[F]): F[String] = {
      entityBody.map(_.toChar).compile.toList.map(_.mkString)
    }
  }
}
