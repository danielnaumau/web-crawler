package com.teamextn.http

import com.teamextn.http.Models.CrawlResponse._
import org.http4s.Uri

object Models {

  sealed trait CrawlResponse

  object CrawlResponse {
    final case class Success(data: String, url: Uri) extends CrawlResponse
    final case class Error(msg: String, url: Uri)    extends CrawlResponse
  }

  final case class CrawlBody(urls: List[Uri])
  final case class ProcessedUrls(results: List[Success], errors: List[Error])

}
