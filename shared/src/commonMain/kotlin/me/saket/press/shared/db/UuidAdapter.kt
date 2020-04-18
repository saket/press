package me.saket.press.shared.db

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.squareup.sqldelight.ColumnAdapter

class UuidAdapter : ColumnAdapter<Uuid, String> {
  override fun decode(databaseValue: String) = uuidFrom(databaseValue)
  override fun encode(value: Uuid) = value.toString()
}
