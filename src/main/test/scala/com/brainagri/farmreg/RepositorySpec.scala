package com.brainagri.farmreg

import org.scalatest.funsuite.AsyncFunSuite
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import slick.jdbc.PostgresProfile.api._
import com.brainagri.farmreg.db.DatabaseModule
import java.nio.file.Paths

class RepositorySpec extends AsyncFunSuite with ForAllTestContainer {
  override val container = PostgreSQLContainer(databaseName = "farmreg", username = "postgres", password = "postgres")

  test("Migrations executam com sucesso") {
    val url = container.jdbcUrl
    sys.props += ("JDBC_URL" -> url)
    sys.props += ("DB_USER" -> "postgres")
    sys.props += ("DB_PASSWORD" -> "postgres")
    DatabaseModule.migrate()
    succeed
  }
}
