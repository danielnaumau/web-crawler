package com.teamextn.http

import com.teamextn.http.Models.CrawlResponse._
import java.net.URL

object Models {

  sealed trait CrawlResponse

  object CrawlResponse {
    final case class Success(data: String, url: URL) extends CrawlResponse
    final case class Error(msg: String, url: URL)    extends CrawlResponse
  }

  final case class CrawlBody(urls: List[URL])
  final case class ProcessedUrls(results: List[Success], errors: List[Error])

}
