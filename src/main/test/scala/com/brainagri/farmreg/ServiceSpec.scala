package com.brainagri.farmreg

import org.scalatest.funsuite.AsyncFunSuite
import com.brainagri.farmreg.service._
import com.brainagri.farmreg.repository._
import com.brainagri.farmreg.model._
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID

class ServiceSpec extends AsyncFunSuite {
  test("Regra de área deve falhar se exceder total") {
    val db = Database.forConfig("slick.db")
    val farmRepo = new FarmRepository(db)
    val svc = new FarmService(farmRepo)
    recoverToSucceededIf[IllegalArgumentException] {
      svc.create(UUID.randomUUID(), CreateFarmIn("Faz","Uberlândia","MG", 100, 60, 50))
    }
  }
}
