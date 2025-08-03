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

# 🚀 Quick Start

### Prerequisites

- Java 21 (OpenJDK)
- Docker & Docker Compose
- Git

### Steps

- The first 4 steps will create the services that the system supports, the following sections will tackle 1 by 1 the tasks given:

1.	Clone the repo, clean possible old outputs and compile code

```
git clone [<repository-url>](https://github.com/sfandino/bank-core-fc.git) && cd bank-transaction-core
```

- Here we clean and compile code - we create artifacts here for later execution
```
./gradlew :app:clean :app:build
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


## CSV Importer

The system supports importing transaction data via CSV file - (it writes the data in PostgreSQL)

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

## Queue Data Ingestion
The system supports streaming via Kafka topics - the sink is the PostgreSQL data base.

7. Creating a Kafka Topic and Publish a Test message

- Creating the topic

```
docker exec -it bankcore-kafka \
  kafka-topics --create \
    --topic transactions \
    --bootstrap-server localhost:9092 \
    --replication-factor 1 \
    --partitions 1
```
- The consumer needs to be started to listen to any incoming message

```
./gradlew :app:runKafkaConsumer
```
- Publish a message / In real life we would have a data stream connected sending this kind of messages

```
docker exec -i bankcore-kafka kafka-console-producer \
  --topic transactions \
  --bootstrap-server localhost:9092 <<EOF
{"transaction_id":"g7a1e5b1-01a2-d4e3-f8a9-b1c2d3e4f4t6","sender_id":"8b2f3c21-2d3e-5f4a-9b8c-2d3e4f5a6b7c","receiver_id":"7a1e2b10-1c2d-4e3f-8a9b-1c2d3e4f5a6b","amount":50000.00,"currency":"USD","timestamp":"2025-08-03T18:00:00Z","status":"completed"}
EOF
```
- This row should be inserted into PostgreSQL, and also should have triggered the warning for suspicious transaction.



## Trouble Shooting

- Always makesure that your code compiles after building the app

```
./gradlew :app:build
```

- You can also makesure that the ingestion tasks are being listed with this command

```
./gradlew :app:tasks --group ingestion
```