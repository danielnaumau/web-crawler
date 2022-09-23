package com.teamextn.http.dsl

import cats.Parallel
import io.circe.generic.auto._
import cats.effect.kernel.Async
import cats.implicits._
import com.teamextn.http.CrawlerClient
import com.teamextn.http.Models.CrawlResponse
import com.teamextn.http.Models.CrawlResponse._
import com.teamextn.http.Models.InMessage.CrawlBody
import com.teamextn.http.Models.OutMessage.ProcessedUrls
import org.http4s.circe.jsonOf
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

final class CrawlerDsl[F[_]: Async: Parallel: Logger](crawlerClient: CrawlerClient[F]) extends Http4sDsl[F] {
  private implicit val crawlBodyDecoder: EntityDecoder[F, CrawlBody] = jsonOf[F, CrawlBody]
  def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root =>
        for {
          body <- req.as[CrawlBody]
          res  <- crawlUrls(body.urls)
        } yield res
    }

  def crawlUrls(urls: List[Uri]): F[ProcessedUrls] = {
    urls.parTraverse(crawlerClient.get).map(foldRequest)
  }

  def foldRequest(responses: List[CrawlResponse]): ProcessedUrls = {
    responses.foldLeft(ProcessedUrls(List.empty, List.empty)) { (result, response) =>
      import result._
      response match {
        case success: Success => result.copy(results = results :+ success)
        case error: Error     => result.copy(errors = errors :+ error)
      }
    }
  }
}
