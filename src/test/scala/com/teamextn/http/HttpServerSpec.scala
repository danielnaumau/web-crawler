package com.teamextn.http

import cats.effect.IO
import com.teamextn.http.Models._
import io.circe.Json
import org.http4s.circe._
import org.http4s.{Method, Request}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HttpServerSpec extends AnyFlatSpec with HttpServerFixtures with Matchers {
  it should "successfully crawl all existing urls" in {
    val crawlRequest = CrawlRequest(urls = List(successUrl1, successUrl2, successUrl3))
    val request      = Request[IO](method = Method.POST, uri = crawlUri).withEntity[CrawlRequest](crawlRequest)

    val response      = sendRequest(request)
    val processedUrls = decode[ProcessedUrls](response)

    processedUrls.results.size shouldBe 3
    response.status.code shouldBe 200
  }

  it should "return errors for non existing urls" in {
    val crawlRequest = CrawlRequest(urls = List(successUrl1, successUrl2, failedUrl1, failedUrl2))
    val request      = Request[IO](method = Method.POST, uri = crawlUri).withEntity[CrawlRequest](crawlRequest)

    val response      = sendRequest(request)
    val processedUrls = decode[ProcessedUrls](response)

    processedUrls.results.size shouldBe 2
    processedUrls.errors.size shouldBe 2
    response.status.code shouldBe 200
  }

  it should "return 422 status code for invalid post body" in {
    val request  = Request[IO](method = Method.POST, uri = crawlUri).withEntity[Json](Json.obj())
    val response = sendRequest(request)

    response.status.code shouldBe 422
  }
}
