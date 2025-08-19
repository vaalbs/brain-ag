ThisBuild / scalaVersion := "2.13.14"
ThisBuild / organization := "com.brainagri"
ThisBuild / version      := "0.1.0"

lazy val http4sVersion = "0.23.26"
lazy val circeVersion  = "0.14.10"

lazy val root = (project in file(".")).
  settings(
    name := "brainagri-farmreg-backend",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl"          % http4sVersion,
      "org.http4s" %% "http4s-circe"        % http4sVersion,
      "io.circe"   %% "circe-generic"       % circeVersion,
      "io.circe"   %% "circe-parser"        % circeVersion,

      "com.typesafe.slick" %% "slick"          % "3.5.1",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.5.1",
      "org.postgresql"      % "postgresql"     % "42.7.3",
      "org.flywaydb"        % "flyway-core"    % "10.17.3",

      "ch.qos.logback" % "logback-classic" % "1.5.6",

      "org.scalatest"          %% "scalatest"                 % "3.2.19" % Test,
      "com.dimafeng"           %% "testcontainers-scala-postgresql" % "0.41.4" % Test,
      "org.testcontainers"      % "postgresql"                % "1.20.1" % Test
    ),
    Compile / mainClass := Some("com.brainagri.farmreg.Main")
  )