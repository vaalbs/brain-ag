## 🚀 Como rodar

### 1) Subir Postgres via Docker Compose

```bash
docker compose up -d postgres
```

### 2) Rodar a aplicação em dev (hot reload opcional)

```bash
sbt ~reStart       
# ou
sbt run             
```

A aplicação sobe em `http://localhost:8080`.

### 3) Rodar com Docker (app + db)

```bash
docker compose up --build
```

App em `http://localhost:8080` e Swagger em `brain-ag-api.yaml` na raiz do projeto.

### 4) Testes

```bash
sbt test
```

---

## 🔗 Endpoints (resumo)

- **Produtores**
  - `POST /producers` — cria produtor (CPF ou CNPJ válidos, únicos)
  - `GET /producers` — lista paginada
  - `GET /producers/{id}` — busca por id
  - `PUT /producers/{id}` — edita
  - `DELETE /producers/{id}` — exclui (em cascata)

- **Propriedades (Fazendas)**
  - `POST /producers/{producerId}/farms` — cria fazenda (regra: agricultável + vegetação ≤ total)
  - `GET /producers/{producerId}/farms` — lista fazendas do produtor
  - `PUT /farms/{farmId}` — edita
  - `DELETE /farms/{farmId}` — exclui

- **Culturas e Safras**
  - `POST /farms/{farmId}/plantings` — cadastra culturas por safra
    ```json
    {
      "season": "Safra 2022",
      "crops": ["Soja", "Milho"]
    }
    ```
  - `GET /farms/{farmId}/plantings?season=Safra%202022` — lista culturas por safra

- **Dashboard**
  - `GET /dashboard` — retorna:
    ```json
    {
      "totalFarms": 12,
      "totalHectares": 13450.5,
      "byState": [{"state":"MG","farms":5,"hectares":5820.0}],
      "byCrop": [{"crop":"Soja","farms":8}],
      "landUse": {"arable": 8900.0, "vegetation": 3150.0}
    }
    ```

> Especificação completa: ver **brain-ag-api.yaml**.