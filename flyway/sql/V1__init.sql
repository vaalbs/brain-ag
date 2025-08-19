CREATE TABLE producers (
  id UUID PRIMARY KEY,
  doc_type VARCHAR(5) NOT NULL CHECK (doc_type IN ('CPF','CNPJ')),
  doc_number VARCHAR(20) NOT NULL UNIQUE,
  name TEXT NOT NULL
);

CREATE TABLE farms (
  id UUID PRIMARY KEY,
  producer_id UUID NOT NULL REFERENCES producers(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  city TEXT NOT NULL,
  state CHAR(2) NOT NULL,
  total_area NUMERIC(15,2) NOT NULL CHECK (total_area >= 0),
  arable_area NUMERIC(15,2) NOT NULL CHECK (arable_area >= 0),
  vegetation_area NUMERIC(15,2) NOT NULL CHECK (vegetation_area >= 0),
  CONSTRAINT chk_area_sum CHECK (arable_area + vegetation_area <= total_area)
);

CREATE TABLE crops (
  id UUID PRIMARY KEY,
  name TEXT NOT NULL UNIQUE
);

CREATE TABLE plantings (
  id UUID PRIMARY KEY,
  farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
  season TEXT NOT NULL,
  crop_id UUID NOT NULL REFERENCES crops(id) ON DELETE RESTRICT,
  CONSTRAINT uq_farm_season_crop UNIQUE (farm_id, season, crop_id)
);

CREATE INDEX idx_farms_state ON farms(state);
CREATE INDEX idx_plantings_season ON plantings(season);
