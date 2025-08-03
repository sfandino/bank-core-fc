package com.bankcore.payments

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class TransactionEvent(
    @JsonProperty("transaction_id") val transactionId: String,
    @JsonProperty("sender_id")      val senderId: String,
    @JsonProperty("receiver_id")    val receiverId: String,
    val amount: BigDecimal,
    val currency: String,
    val timestamp: String,
    val status: String
)