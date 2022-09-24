package com.teamextn.http

import io.circe._

import java.net.URL
import scala.util.Try

object Codecs {
  implicit val encodeURL: Encoder[URL] = Encoder.encodeString.contramap[URL](_.toString)
  implicit val decodeURL: Decoder[URL] = Decoder.decodeString.emapTry(str => Try(new URL(str)))
}
