package com.teamextn.http

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.teamextn.AppConfig.HttpConfig
import com.teamextn.http.Codecs._
import com.teamextn.http.Models._
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.{EntityDecoder, EntityEncoder, Request, Response, Uri}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import org.typelevel.log4cats.SelfAwareLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.net.URL

trait HttpServerFixtures {
  val crawlUri = Uri.unsafeFromString("/api/crawl")

  val crawlerClient = mock[CrawlerClient[IO]]

  val successUrl1 = new URL("https://test1.com")
  when(crawlerClient.get(successUrl1)).thenReturn(IO { CrawlResponse.Success("", successUrl1) })

  val successUrl2 = new URL("https://test2.com")
  when(crawlerClient.get(successUrl2)).thenReturn(IO { CrawlResponse.Success("", successUrl2) })

  val successUrl3 = new URL("https://test3.com")
  when(crawlerClient.get(successUrl3)).thenReturn(IO { CrawlResponse.Success("", successUrl3) })

  val failedUrl1 = new URL("https://failed1.com")
  when(crawlerClient.get(failedUrl1)).thenReturn(IO { CrawlResponse.Error("", failedUrl1) })

  val failedUrl2 = new URL("https://failed2.com")
  when(crawlerClient.get(failedUrl2)).thenReturn(IO { CrawlResponse.Error("", failedUrl2) })

  implicit val crawlRequestEncoder: EntityEncoder[IO, CrawlRequest]   = jsonEncoderOf[IO, CrawlRequest]
  implicit val processedUrlsDecoder: EntityDecoder[IO, ProcessedUrls] = jsonOf[IO, ProcessedUrls]

  implicit val catsLogger: SelfAwareLogger[IO] = Slf4jLogger.getLogger[IO]

  val httpServer = HttpServer(HttpConfig("0.0.0.0", 8080), crawlerClient)

  def decode[T](response: Response[IO])(implicit decoder: EntityDecoder[IO, T]): T =
    response.as[T].unsafeRunSync()

  def sendRequest(request: Request[IO]): Response[IO] =
    httpServer.routes(request).value.unsafeRunSync().get

}
