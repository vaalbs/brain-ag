package com.brainagri.farmreg.api

import io.circe._
import io.circe.generic.semiauto._
import com.brainagri.farmreg.model._

object JsonCodecs {
  implicit val byStateEnc: Encoder[ByState] = deriveEncoder
  implicit val byCropEnc:  Encoder[ByCrop]  = deriveEncoder
  implicit val landUseEnc: Encoder[LandUse] = deriveEncoder
  implicit val dashboardEnc: Encoder[Dashboard] = deriveEncoder

  implicit val createProducerDec: Decoder[CreateProducerIn] = deriveDecoder[CreateProducerIn]
  implicit val updateProducerDec: Decoder[UpdateProducerIn] = deriveDecoder[UpdateProducerIn]

  implicit val createFarmDec: Decoder[CreateFarmIn] = deriveDecoder[CreateFarmIn]
  implicit val updateFarmDec: Decoder[UpdateFarmIn] = deriveDecoder[UpdateFarmIn]

  implicit val plantingsInDec: Decoder[PlantingsIn] = deriveDecoder[PlantingsIn]

  implicit val producerEnc: Encoder[Producer] = new Encoder[Producer] {
    def apply(p: Producer): Json = Json.obj(
      "id" -> Json.fromString(p.id.toString),
      "docType" -> Json.fromString(p.docType.value),
      "docNumber" -> Json.fromString(p.docNumber),
      "name" -> Json.fromString(p.name)
    )
  }

  implicit val farmEnc: Encoder[Farm] = new Encoder[Farm] {
    def apply(f: Farm): Json = Json.obj(
      "id" -> Json.fromString(f.id.toString),
      "producerId" -> Json.fromString(f.producerId.toString),
      "name" -> Json.fromString(f.name),
      "city" -> Json.fromString(f.city),
      "state" -> Json.fromString(f.state),
      "totalArea" -> Json.fromBigDecimal(f.totalArea),
      "arableArea" -> Json.fromBigDecimal(f.arableArea),
      "vegetationArea" -> Json.fromBigDecimal(f.vegetationArea)
    )
  }

  implicit val plantingEnc: Encoder[Planting] = new Encoder[Planting] {
    def apply(p: Planting): Json = Json.obj(
      "id" -> Json.fromString(p.id.toString),
      "farmId" -> Json.fromString(p.farmId.toString),
      "season" -> Json.fromString(p.season),
      "cropId" -> Json.fromString(p.cropId.toString)
    )
  }
}
