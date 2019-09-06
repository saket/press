package me.saket.compose.shared.db

import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeTz
import com.squareup.sqldelight.ColumnAdapter

class DateTimeTzAdapter : ColumnAdapter<DateTimeTz, String> {
  override fun decode(databaseValue: String) = DateTime.fromString(databaseValue)
  override fun encode(value: DateTimeTz) = value.toString()
}