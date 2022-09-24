package com.teamextn.http.dsl

import cats.Parallel
import io.circe.generic.auto._
import cats.effect.kernel.Async
import cats.implicits._
import com.teamextn.http.CrawlerClient
import com.teamextn.http.Models._
import com.teamextn.http.Models.CrawlResponse._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

final class CrawlerDsl[F[_]: Async: Parallel: Logger](crawlerClient: CrawlerClient[F]) extends Http4sDsl[F] {
  private implicit val processedUrlsEncoder: EntityEncoder[F, ProcessedUrls] = jsonEncoderOf[F, ProcessedUrls]
  private implicit val crawlBodyDecoder: EntityDecoder[F, CrawlBody]         = jsonOf[F, CrawlBody]

  def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root =>
        for {
          body   <- req.as[CrawlBody]
          res    <- crawlUrls(body.urls)
          status <- Ok(res)
        } yield status
    }

  def crawlUrls(urls: List[Uri]): F[ProcessedUrls] =
    urls.parTraverse(crawlerClient.get).map(foldRequest)

  def foldRequest(responses: List[CrawlResponse]): ProcessedUrls =
    responses.foldLeft(ProcessedUrls(List.empty, List.empty)) { (processedUrls, response) =>
      response match {
        case success: Success => processedUrls.copy(results = processedUrls.results :+ success)
        case error: Error     => processedUrls.copy(errors = processedUrls.errors :+ error)
      }
    }
}
