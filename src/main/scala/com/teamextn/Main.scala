package com.teamextn

import cats.effect.{ExitCode, IO, IOApp}
import com.teamextn.http.CrawlerClient
import org.http4s.Uri
import org.http4s.blaze.client.BlazeClientBuilder
import org.typelevel.log4cats.SelfAwareLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.nio.charset.StandardCharsets
import scala.concurrent.duration.DurationInt

object Main extends IOApp {
  implicit private val catsLogger: SelfAwareLogger[IO] = Slf4jLogger.getLogger[IO]

  private val httpClient = BlazeClientBuilder[IO].resource

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      config <- AppConfig.load[IO]
      _      <- catsLogger.info(s"Loaded config: $config")

      crawlerClient = httpClient.map(CrawlerClient.apply[IO])
      _             <- crawlerClient.use(test)
    } yield ExitCode.Success
  }

  def test(crawlerClient: CrawlerClient[IO]) = {
    val uri = Uri.unsafeFromString("https://github.com")

    crawlerClient.get(uri).map(println)
  }

  //new String(bytes, StandardCharsets.UTF_8)
}
