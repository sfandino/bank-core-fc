package com.bankcore.payments

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime

object Users : Table("users") {
  val id        = uuid("id").primaryKey()
  val name      = text("name")
  val email     = text("email").uniqueIndex()
  val createdAt = datetime("created_at")
}

object Currencies : Table("currencies") {
  val code      = varchar("code", 3).primaryKey()
  val name      = text("name")
  val symbol    = text("symbol").nullable()
  val createdAt = datetime("created_at")
}

object Transactions : Table("transactions") {
  val id           = uuid("id").primaryKey()
  val senderId     = uuid("sender_id").references(Users.id)
  val receiverId   = uuid("receiver_id").references(Users.id)
  val currencyCode = varchar("currency_code", 3).references(Currencies.code)
  val amount       = decimal("amount", 12, 2)
  val status       = varchar("status", 10)
  val occurredAt   = datetime("occurred_at")
  val createdAt    = datetime("created_at")
}