package com.brainagri.farmreg.api

import cats.effect._
import cats.syntax.all._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import org.http4s.circe._
import io.circe.{Decoder, Json}
import io.circe.syntax._
import com.brainagri.farmreg.service._
import com.brainagri.farmreg.model._
import org.http4s.HttpRoutes
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

object OffsetMatcher extends OptionalQueryParamDecoderMatcher[Int]("offset")
object LimitMatcher  extends OptionalQueryParamDecoderMatcher[Int]("limit")
object SeasonMatcher extends OptionalQueryParamDecoderMatcher[String]("season")

class Routes(
  producerSvc: ProducerService,
  farmSvc: FarmService,
  plantingSvc: PlantingService
) extends Http4sDsl[IO] {
  import JsonCodecs._

  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit val jsonEntityDecoder: EntityDecoder[IO, Json] = jsonOf[IO, Json]

  private def fut[A](fa: Future[A]): IO[A] = IO.fromFuture(IO(fa))

  private def decode[A: Decoder](req: Request[IO]): IO[A] =
    req.as[Json].flatMap { j =>
      implicitly[Decoder[A]].decodeJson(j).fold(
        err => IO.raiseError(new Exception(err.toString)),
        ok  => IO.pure(ok)
      )
    }

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root / "health" => Ok("ok")

    case req @ POST -> Root / "producers" =>
      decode[CreateProducerIn](req).flatMap { in =>
        fut(producerSvc.create(in)).flatMap(p => Created(p.asJson))
          .handleErrorWith(e => BadRequest(e.getMessage))
      }

    case GET -> Root / "producers" :? OffsetMatcher(offset) +& LimitMatcher(limit) =>
      val off = offset.getOrElse(0)
      val lim = limit.getOrElse(50)
      fut(producerSvc.list(off, lim)).flatMap(ps => Ok(ps.asJson))

    case GET -> Root / "producers" / UUIDVar(id) =>
      fut(producerSvc.get(id)).flatMap {
        case Some(p) => Ok(p.asJson)
        case None    => NotFound("Produtor não encontrado")
      }

    case req @ PUT -> Root / "producers" / UUIDVar(id) =>
      decode[UpdateProducerIn](req)
        .flatMap(in => fut(producerSvc.updateName(id, in.name)) *> NoContent())
        .handleErrorWith(e => BadRequest(e.getMessage))

    case DELETE -> Root / "producers" / UUIDVar(id) =>
      fut(producerSvc.delete(id)) *> NoContent()

    case req @ POST -> Root / "producers" / UUIDVar(pid) / "farms" =>
      decode[CreateFarmIn](req).flatMap { in =>
        fut(farmSvc.create(pid, in)).flatMap(f => Created(f.asJson))
          .handleErrorWith(e => BadRequest(e.getMessage))
      }

    case GET -> Root / "producers" / UUIDVar(pid) / "farms" =>
      fut(farmSvc.listByProducer(pid)).flatMap(fs => Ok(fs.asJson))

    case req @ PUT -> Root / "farms" / UUIDVar(fid) =>
      decode[UpdateFarmIn](req)
        .flatMap(in => fut(farmSvc.update(fid, in)) *> NoContent())
        .handleErrorWith(e => BadRequest(e.getMessage))

    case DELETE -> Root / "farms" / UUIDVar(fid) =>
      fut(farmSvc.delete(fid)) *> NoContent()

    case req @ POST -> Root / "farms" / UUIDVar(fid) / "plantings" =>
      decode[PlantingsIn](req).flatMap { in =>
        fut(plantingSvc.addPlantings(fid, in.season, in.crops)) *> Created()
      }.handleErrorWith(e => BadRequest(e.getMessage))

    case GET -> Root / "farms" / UUIDVar(fid) / "plantings" :? SeasonMatcher(season) =>
      fut(plantingSvc.listPlantings(fid, season)).flatMap(ps => Ok(ps.asJson))

    case GET -> Root / "dashboard" =>
      fut(farmSvc.dashboard()).flatMap(base => Ok(base.asJson))
  }
}
