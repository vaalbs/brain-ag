package com.brainagri.farmreg

import cats.effect._
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware._
import com.brainagri.farmreg.db.DatabaseModule
import com.brainagri.farmreg.repository._
import com.brainagri.farmreg.service._
import com.brainagri.farmreg.api.Routes
import scala.concurrent.ExecutionContext

object Main extends IOApp.Simple {
  def run: IO[Unit] = {
    DatabaseModule.migrate()

    implicit val ec: ExecutionContext = ExecutionContext.global
    val db = DatabaseModule.db

    val prodRepo = new ProducerRepository(db)
    val farmRepo = new FarmRepository(db)
    val cropRepo = new CropRepository(db)
    val plantRepo = new PlantingRepository(db)

    val prodSvc = new ProducerService(prodRepo)
    val farmSvc = new FarmService(farmRepo)
    val plantSvc = new PlantingService(farmRepo, cropRepo, plantRepo)

    val routes = new com.brainagri.farmreg.api.Routes(prodSvc, farmSvc, plantSvc)

    val httpApp = (
      CORS.policy.withAllowOriginAll(
        Logger.httpApp(true, true)(
          GZip(routes.routes.orNotFound)
        )
      )
    )

    EmberServerBuilder.default[IO]
      .withHost(host"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
      .useForever
  }
}
