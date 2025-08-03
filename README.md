# Bank Transaction Core

A simple Kotlin application to manage payment transactions, supporting database migrations and CSV ingestion.

### 🌐 Overview

- Language: Kotlin (JVM)
- Database: PostgreSQL (via Docker)
- Migration: Flyway (via Kotlin runner)
- Ingestion: CSV import using JDBC + HikariCP

### 📦 Database Schema

• The schema or DDL of the data base is created under the Flyway repository folder:
[Please find it here!](bank-core-fc/bank-transaction-core/app/src/main/resources/db/migration/V1__init.sql)
    
• Flyway tracks applied migrations in flyway_schema_history, so for the V1 and V2 we split definition of tables and insert statements

### 📄 CSV File Format

•	[Find it here!](app/src/main/resources/data/transactions.csv)
•	Columns (header row required):
	1.	transaction_id (string 8-4-4-4-12)
	2.	sender_id      (string 8-4-4-4-12)
	3.	receiver_id    (string 8-4-4-4-12)
	4.	amount         (decimal)
	5.	currency       (3-letter code)
	6.	timestamp      (ISO-8601 with Z)
	7.	status         (pending|completed|failed)

Rows with invalid UUIDs are skipped; amounts >10,000 are flagged as suspicious in the logs and also True for flag_suspicious.

## 🚀 Quick Start

### Prerequisites

- Java 21 (OpenJDK)
- Docker & Docker Compose
- Git

### Steps

1.	Clone the repo

```
git clone [<repository-url>](https://github.com/sfandino/bank-core-fc.git) && cd bank-transaction-core
```

3.	Start PostgreSQL + PgAdmin

```
docker compose up -d	
```

4.	Apply database migrations

```
./gradlew :app:flywayMigrateManual
```

- Creates users, currencies, transactions tables and also inserts User and currency data

5.	Import transactions from CSV

```
./gradlew :app:importCsv
```

- Reads transactions.csv, inserts into transactions table,
	skips duplicates, flags suspicious amounts.

6.	Monitor logs
- Console: output appears during importCsv task run
- File: tail -f logs/import.log
- [Read also here!](bank-core-fc/bank-transaction-core/app/logs/import.log)
