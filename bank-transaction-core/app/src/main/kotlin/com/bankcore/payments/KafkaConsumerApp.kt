package com.bankcore.payments

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*

private val logger = KotlinLogging.logger {}

fun main() {
    // 1) Kafka config
    val props = Properties().apply {
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        put(ConsumerConfig.GROUP_ID_CONFIG, "bankcore-consumer")
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
    }

    val consumer = KafkaConsumer<String, String>(props).apply {
        subscribe(listOf("transactions"))
    }

    val mapper = jacksonObjectMapper()
    val ds     = HikariProvider.dataSource()

    logger.info { "Kafka consumer started, listening to topic 'transactions'" }

    // 2) Poll for messages
    while (true) {
        val records = consumer.poll(Duration.ofSeconds(1))
        for (record in records) {
            val json = record.value()
            try {
                val evt: TransactionEvent = mapper.readValue(json)
                val ok = JdbcService.insertEvent(ds, evt)
                if (ok) {
                    consumer.commitSync() 
                }
            } catch (e: Exception) {
                logger.error(e) { "Wrong JSON, skipping: $json" }
            }
        }
    }
}