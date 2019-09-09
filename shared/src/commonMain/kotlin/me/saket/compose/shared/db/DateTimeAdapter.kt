package me.saket.compose.shared.db

import com.soywiz.klock.DateTime
import com.squareup.sqldelight.ColumnAdapter

/**
 * For UTC date times.
 */
class DateTimeAdapter : ColumnAdapter<DateTime, String> {
  override fun decode(databaseValue: String) = DateTime.fromString(databaseValue).utc
  override fun encode(value: DateTime) = value.toString()
}