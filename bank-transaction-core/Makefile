.PHONY: up down down-volumes clean build migrate ingest-csv consume schedule ingest-json report bootstrap
# Composer commands
up:
	docker compose up -d
	./gradlew clean
	./gradlew build
	./gradlew :app:flywayMigrateManual
	./gradlew :app:runQuartzScheduler
	./gradlew :app:runKafkaConsumer

down:
	docker compose down

down-volumes:
	docker compose down -v

# Gradle commands
clean:
	./gradlew clean

build:
	./gradlew build

migrate:
	./gradlew :app:flywayMigrateManual

ingest-csv:
	./gradlew :app:importCsv

consume:
	./gradlew :app:runKafkaConsumer

schedule:
	./gradlew :app:runQuartzScheduler

ingest-json:
	docker exec -i bankcore-kafka kafka-console-producer \
	  --topic transactions \
	  --bootstrap-server localhost:9092 <<EOF{"transaction_id":"g7a1e5b1-01a2-d4e3-f8a9-b1c2d3e4f4t6","sender_id":"8b2f3c21-2d3e-5f4a-9b8c-2d3e4f5a6b7c","receiver_id":"7a1e2b10-1c2d-4e3f-8a9b-1c2d3e4f5a6b","amount":50000.00,"currency":"USD","timestamp":"2025-08-03T18:00:00Z","status":"completed"}EOF

report:
	./gradlew :app:report --args="$(ARGS)"
# Example usage:
# make report ARGS="payments 8e2f3a10-2a3b-5c4d-7e6f-2a3b4c5d6e7f 2025-04-01T00:00:00Z 2025-08-04T00:00:00Z"
# make report ARGS="all-users-balance 2025-05-03"
# make report ARGS="daily-totals 8e2f3a10-2a3b-5c4d-7e6f-2a3b4c5d6e7f 2022-04-01 2025-08-03"
