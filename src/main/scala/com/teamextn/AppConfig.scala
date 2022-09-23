package com.teamextn

import cats.effect.Sync
import AppConfig._
import pureconfig.ConfigSource
import pureconfig.generic.auto._

final case class AppConfig(http: HttpConfig)

object AppConfig {
  final case class HttpConfig(host: String, port: Int)

  def load[F[_]: Sync]: F[AppConfig] = Sync[F].delay(ConfigSource.default.loadOrThrow[AppConfig])
}
