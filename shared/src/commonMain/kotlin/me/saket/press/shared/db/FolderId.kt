package me.saket.press.shared.db

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import com.squareup.sqldelight.ColumnAdapter
import me.saket.press.shared.AndroidParcel
import me.saket.press.shared.AndroidParcelize

// Inline class would have been nice if it was supported by,
// - kotlin multiplatform
// - kotlinx.serialization
@AndroidParcelize
data class FolderId(val value: Uuid) : AndroidParcel {
  override fun toString() = "FolderId($value)"

  object SqlAdapter : ColumnAdapter<FolderId, String> {
    override fun decode(databaseValue: String) = FolderId(uuidFrom(databaseValue))
    override fun encode(value: FolderId) = value.value.toString()
  }

  companion object {
    fun generate() = FolderId(uuid4())
  }
}
