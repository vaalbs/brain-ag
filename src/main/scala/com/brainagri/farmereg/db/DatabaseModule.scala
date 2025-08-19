package com.brainagri.farmreg.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import slick.jdbc.JdbcBackend.Database

object DatabaseModule {
  lazy val dataSource: HikariDataSource = {
    val cfg = new HikariConfig()
    val url  = sys.env.getOrElse("JDBC_URL",  "jdbc:postgresql://localhost:5432/farmreg")
    val user = sys.env.getOrElse("DB_USER",    "postgres")
    val pass = sys.env.getOrElse("DB_PASSWORD","postgres")

    cfg.setJdbcUrl(url)
    cfg.setUsername(user)
    cfg.setPassword(pass)
    cfg.setMaximumPoolSize(10)
    new HikariDataSource(cfg)
  }

  lazy val db: Database = Database.forDataSource(dataSource, Some(10))

  def migrate(): Unit = {
    val flyway = Flyway.configure()
      .dataSource(dataSource)
      .locations("filesystem:flyway/sql")
      .baselineOnMigrate(true)
      .load()
    flyway.migrate()
  }
}
