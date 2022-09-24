package com.teamextn.http.dsl

import cats.Parallel
import io.circe.generic.auto._
import cats.effect.kernel.Async
import cats.implicits._
import com.teamextn.http.CrawlerClient
import com.teamextn.http.Codecs._
import com.teamextn.http.Models._
import com.teamextn.http.Models.CrawlResponse._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

import java.net.URL

final class CrawlerDsl[F[_]: Async: Parallel: Logger](crawlerClient: CrawlerClient[F]) extends Http4sDsl[F] {
  private implicit val processedUrlsEncoder: EntityEncoder[F, ProcessedUrls] = jsonEncoderOf[F, ProcessedUrls]
  private implicit val crawlRequestDecoder: EntityDecoder[F, CrawlRequest]   = jsonOf[F, CrawlRequest]

  def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root =>
        decode[CrawlRequest](req)(crawlReq => crawlUrls(crawlReq.urls).flatMap(Ok(_)))
    }

  private def decode[T](req: Request[F])(f: T => F[Response[F]])(
      implicit decoder: EntityDecoder[F, T]
  ): F[Response[F]] =
    req
      .attemptAs[T]
      .foldF(
        failure => UnprocessableEntity(failure.getLocalizedMessage),
        processed => f(processed)
      )

  private def crawlUrls(urls: List[URL]): F[ProcessedUrls] =
    urls.parTraverse(crawlerClient.get).map(foldRequest)

  private def foldRequest(responses: List[CrawlResponse]): ProcessedUrls =
    responses.foldLeft(ProcessedUrls(List.empty, List.empty)) { (processedUrls, response) =>
      response match {
        case success: Success => processedUrls.copy(results = processedUrls.results :+ success)
        case error: Error     => processedUrls.copy(errors = processedUrls.errors :+ error)
      }
    }
}
