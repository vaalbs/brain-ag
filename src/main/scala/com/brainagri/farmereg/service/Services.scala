package com.brainagri.farmreg.db

import com.brainagri.farmreg.model._
import slick.jdbc.PostgresProfile.api._
import java.util.UUID

class Producers(tag: Tag) extends Table[Producer](tag, "producers") {
  def id         = column[UUID]("id", O.PrimaryKey)
  def docTypeStr = column[String]("doc_type")
  def docNumber  = column[String]("doc_number")
  def name       = column[String]("name")

  private def toDoc(s: String) = DocType.fromString(s).get

  def * = (id, docTypeStr, docNumber, name) <> (
    { case (id, dt, dn, n) => Producer(id, toDoc(dt), dn, n) },
    { p: Producer => Some((p.id, p.docType.value, p.docNumber, p.name)) }
  )
  def idxUniqueDoc = index("uq_doc_number", docNumber, unique = true)
}

class Farms(tag: Tag) extends Table[Farm](tag, "farms") {
  def id              = column[UUID]("id", O.PrimaryKey)
  def producerId      = column[UUID]("producer_id")
  def name            = column[String]("name")
  def city            = column[String]("city")
  def state           = column[String]("state")
  def totalArea       = column[BigDecimal]("total_area")
  def arableArea      = column[BigDecimal]("arable_area")
  def vegetationArea  = column[BigDecimal]("vegetation_area")

  def * = (id, producerId, name, city, state, totalArea, arableArea, vegetationArea) <> (Farm.tupled, Farm.unapply)
  def producerFk = foreignKey("fk_farm_producer", producerId, TableQuery[Producers])(_.id, onDelete = ForeignKeyAction.Cascade)
  def idxState   = index("idx_farms_state", state)
}

class Crops(tag: Tag) extends Table[Crop](tag, "crops") {
  def id   = column[UUID]("id", O.PrimaryKey)
  def name = column[String]("name")
  def *    = (id, name) <> (Crop.tupled, Crop.unapply)
  def uq   = index("uq_crop_name", name, unique = true)
}

class Plantings(tag: Tag) extends Table[Planting](tag, "plantings") {
  def id      = column[UUID]("id", O.PrimaryKey)
  def farmId  = column[UUID]("farm_id")
  def season  = column[String]("season")
  def cropId  = column[UUID]("crop_id")

  def * = (id, farmId, season, cropId) <> (Planting.tupled, Planting.unapply)
  def farmFk = foreignKey("fk_plant_farm", farmId, TableQuery[Farms])(_.id, onDelete = ForeignKeyAction.Cascade)
  def cropFk = foreignKey("fk_plant_crop", cropId, TableQuery[Crops])(_.id)
  def uq = index("uq_farm_season_crop", (farmId, season, cropId), unique = true)
}
