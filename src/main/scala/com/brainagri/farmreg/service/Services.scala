package com.brainagri.farmreg.service

import com.brainagri.farmreg.model._
import com.brainagri.farmreg.repository._
import com.brainagri.farmreg.validation.Validators
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class ProducerService(repo: ProducerRepository)(implicit ec: ExecutionContext) {
  def create(in: CreateProducerIn): Future[Producer] = {
    val docType = DocType.fromString(in.docType).getOrElse(throw new IllegalArgumentException("docType inválido (CPF/CNPJ)"))
    val isValid = docType match {
      case DocType.CPF  => Validators.isValidCPF(in.docNumber)
      case DocType.CNPJ => Validators.isValidCNPJ(in.docNumber)
    }
    if (!isValid) throw new IllegalArgumentException("Documento inválido")

    repo.getByDoc(in.docNumber).flatMap {
      case Some(_) => Future.failed(new IllegalArgumentException("Documento já cadastrado"))
      case None    =>
        val p = Producer(UUID.randomUUID(), docType, in.docNumber, in.name)
        repo.create(p).map(_ => p)
    }
  }

  def updateName(id: UUID, name: String): Future[Unit] = repo.updateName(id, name).map(_ => ())
  def get(id: UUID): Future[Option[Producer]] = repo.get(id)
  def list(offset: Int, limit: Int): Future[Seq[Producer]] = repo.list(offset, limit)
  def delete(id: UUID): Future[Unit] = repo.delete(id).map(_ => ())
}

class FarmService(repo: FarmRepository)(implicit ec: ExecutionContext) {
  private def checkAreas(total: BigDecimal, arable: BigDecimal, veg: BigDecimal): Unit = {
    if (arable + veg > total) throw new IllegalArgumentException("Soma de áreas (agricultável + vegetação) não pode ultrapassar a área total")
  }

  def create(pid: UUID, in: CreateFarmIn): Future[Farm] = {
    checkAreas(in.totalArea, in.arableArea, in.vegetationArea)
    val f = Farm(UUID.randomUUID(), pid, in.name, in.city, in.state.toUpperCase.take(2), in.totalArea, in.arableArea, in.vegetationArea)
    repo.create(f).map(_ => f)
  }

  def update(id: UUID, in: UpdateFarmIn): Future[Unit] = {
    checkAreas(in.totalArea, in.arableArea, in.vegetationArea)
    val f = Farm(id, UUID.randomUUID(), in.name, in.city, in.state.toUpperCase.take(2), in.totalArea, in.arableArea, in.vegetationArea)
    repo.get(id).flatMap {
      case Some(existing) =>
        val upd = existing.copy(
          name = in.name, city = in.city, state = in.state.toUpperCase.take(2),
          totalArea = in.totalArea, arableArea = in.arableArea, vegetationArea = in.vegetationArea)
        repo.update(upd).map(_ => ())
      case None => Future.failed(new NoSuchElementException("Fazenda não encontrada"))
    }
  }

  def listByProducer(pid: UUID) = repo.listByProducer(pid)
  def delete(id: UUID): Future[Unit] = repo.delete(id).map(_ => ())

  def dashboard(): Future[Dashboard] = {
    for {
      totalFarms     <- repo.countFarms()
      totalHectares  <- repo.sumTotalHectares()
      (ar, veg)      <- repo.landUse()
      byStateRows    <- repo.byState()
    } yield Dashboard(
      totalFarms = totalFarms,
      totalHectares = totalHectares,
      byState = byStateRows.map { case (st, farms, ha) => ByState(st, farms, ha) }.toList,
      byCrop = Nil,
      landUse = LandUse(ar, veg)
    )
  }
}

class PlantingService(farmRepo: FarmRepository, cropRepo: CropRepository, plantingRepo: PlantingRepository)(implicit ec: ExecutionContext) {
  def addPlantings(farmId: UUID, season: String, cropNames: List[String]): Future[Unit] = {
    farmRepo.get(farmId).flatMap {
      case None => Future.failed(new NoSuchElementException("Fazenda não encontrada"))
      case Some(_) =>
        Future.sequence(cropNames.distinct.map { name =>
          cropRepo.upsertByName(name).flatMap { crop => plantingRepo.add(farmId, season, crop.id).recover { case _ => 0 } }
        }).map(_ => ())
    }
  }

  def listPlantings(farmId: UUID, season: Option[String]) = plantingRepo.listByFarmAndSeason(farmId, season)
}
