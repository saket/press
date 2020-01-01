package me.saket.press.shared.db

import com.benasher44.uuid.Uuid
import com.squareup.sqldelight.ColumnAdapter

class UuidAdapter : ColumnAdapter<Uuid, String> {
  override fun decode(databaseValue: String) = Uuid.parse(databaseValue)!!
  override fun encode(value: Uuid) = value.toString()
}
