package com.brainagri.farmreg.repository

import com.brainagri.farmreg.db._
import com.brainagri.farmreg.model._
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.JdbcBackend.Database
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class ProducerRepository(db: Database)(implicit ec: ExecutionContext) {
  private val producers = TableQuery[Producers]
  def create(p: Producer): Future[Int] = db.run(producers += p)
  def get(id: UUID): Future[Option[Producer]] = db.run(producers.filter(_.id === id).result.headOption)
  def getByDoc(doc: String): Future[Option[Producer]] = db.run(producers.filter(_.docNumber === doc).result.headOption)
  def list(offset: Int, limit: Int): Future[Seq[Producer]] = db.run(producers.drop(offset).take(limit).result)
  def updateName(id: UUID, name: String): Future[Int] = db.run(producers.filter(_.id === id).map(_.name).update(name))
  def delete(id: UUID): Future[Int] = db.run(producers.filter(_.id === id).delete)
}

class FarmRepository(db: Database)(implicit ec: ExecutionContext) {
  private val farms = TableQuery[Farms]
  def create(f: Farm): Future[Int] = db.run(farms += f)
  def listByProducer(pid: UUID): Future[Seq[Farm]] = db.run(farms.filter(_.producerId === pid).result)
  def get(id: UUID): Future[Option[Farm]] = db.run(farms.filter(_.id === id).result.headOption)
  def update(f: Farm): Future[Int] = db.run(farms.filter(_.id === f.id).update(f))
  def delete(id: UUID): Future[Int] = db.run(farms.filter(_.id === id).delete)

  def countFarms(): Future[Long] = db.run(farms.length.result.map(_.toLong))
  def sumTotalHectares(): Future[BigDecimal] = db.run(farms.map(_.totalArea).sum.result.map(_.getOrElse(BigDecimal(0))))
  def landUse(): Future[(BigDecimal, BigDecimal)] =
    db.run(farms.map(f => (f.arableArea, f.vegetationArea)).result)
      .map(_.foldLeft((BigDecimal(0), BigDecimal(0))) { case ((a,v), (aa,va)) => (a+aa, v+va) })

  def byState(): Future[Seq[(String, Long, BigDecimal)]] =
    db.run(farms.groupBy(_.state).map { case (st, g) => (st, g.length, g.map(_.totalArea).sum.getOrElse(BigDecimal(0))) }.result)
      .map(_.map { case (st, cnt, ha) => (st, cnt.toLong, ha) })
}

class CropRepository(db: Database)(implicit ec: ExecutionContext) {
  private val crops = TableQuery[Crops]
  def upsertByName(name: String): Future[Crop] = {
    val id = UUID.randomUUID()
    val insert = DBIO.seq(crops.map(c => (c.id, c.name)) += (id -> name)).andThen(crops.filter(_.name === name).result.head)
    db.run(insert.asTry).flatMap {
      case scala.util.Success(c) => Future.successful(c)
      case scala.util.Failure(_) => db.run(crops.filter(_.name === name).result.head)
    }
  }
}

class PlantingRepository(db: Database)(implicit ec: ExecutionContext) {
  private val plantings = TableQuery[Plantings]
  def add(farmId: UUID, season: String, cropId: UUID): Future[Int] =
    db.run(plantings += Planting(UUID.randomUUID(), farmId, season, cropId))
  def listByFarmAndSeason(farmId: UUID, season: Option[String]): Future[Seq[Planting]] = {
    val base = plantings.filter(_.farmId === farmId)
    val q = season.fold(base)(s => base.filter(_.season === s))
    db.run(q.result)
  }
}
