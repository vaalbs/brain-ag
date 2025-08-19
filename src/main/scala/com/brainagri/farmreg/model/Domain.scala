package com.brainagri.farmreg.model

import java.util.UUID

sealed trait DocType { def value: String }
object DocType {
  case object CPF extends DocType { val value = "CPF" }
  case object CNPJ extends DocType { val value = "CNPJ" }

  def fromString(s: String): Option[DocType] = s.toUpperCase match {
    case "CPF"  => Some(CPF)
    case "CNPJ" => Some(CNPJ)
    case _       => None
  }
}

final case class Producer(
  id: UUID,
  docType: DocType,
  docNumber: String,
  name: String
)

final case class Farm(
  id: UUID,
  producerId: UUID,
  name: String,
  city: String,
  state: String,
  totalArea: BigDecimal,
  arableArea: BigDecimal,
  vegetationArea: BigDecimal
)

final case class Crop(
  id: UUID,
  name: String
)

final case class Planting(
  id: UUID,
  farmId: UUID,
  season: String,
  cropId: UUID
)

final case class CreateProducerIn(docType: String, docNumber: String, name: String)
final case class UpdateProducerIn(name: String)

final case class CreateFarmIn(
  name: String,
  city: String,
  state: String,
  totalArea: BigDecimal,
  arableArea: BigDecimal,
  vegetationArea: BigDecimal
)
final case class UpdateFarmIn(
  name: String,
  city: String,
  state: String,
  totalArea: BigDecimal,
  arableArea: BigDecimal,
  vegetationArea: BigDecimal
)

final case class PlantingsIn(season: String, crops: List[String])

final case class Dashboard(
  totalFarms: Long,
  totalHectares: BigDecimal,
  byState: List[ByState],
  byCrop: List[ByCrop],
  landUse: LandUse
)
final case class ByState(state: String, farms: Long, hectares: BigDecimal)
final case class ByCrop(crop: String, farms: Long)
final case class LandUse(arable: BigDecimal, vegetation: BigDecimal)
