import Dependencies._

ThisBuild / version := "latest"
ThisBuild / scalaVersion := "2.13.8"

enablePlugins(JavaAppPackaging)

dockerRepository := Some("danielnaumau")

lazy val root = (project in file("."))
  .settings(
    name := "web-crawler",
    libraryDependencies ++= Seq(
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.http4sBlazeServer,
      Libraries.http4sCirce,
      Libraries.http4sDsl,
      Libraries.http4sBlazeClient,
      Libraries.pureConfig,
      Libraries.enumeratumCirce,
      Libraries.scalaTest,
      Libraries.mockito,
      Libraries.logback,
      Libraries.slf4jCats,
    )
  )
