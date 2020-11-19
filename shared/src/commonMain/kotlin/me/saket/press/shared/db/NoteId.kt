package me.saket.press.shared.db

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import com.squareup.sqldelight.ColumnAdapter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.saket.press.shared.AndroidParcel
import me.saket.press.shared.AndroidParcelize

// Inline class would have been nice if it was supported by,
// - kotlin multiplatform
// - kotlinx.serialization
@AndroidParcelize
data class NoteId(val value: Uuid) : AndroidParcel {
  override fun toString() = "NoteId($value)"

  object SqlAdapter : ColumnAdapter<NoteId, String> {
    override fun decode(databaseValue: String) = NoteId(uuidFrom(databaseValue))
    override fun encode(value: NoteId) = value.value.toString()
  }

  @Serializer(forClass = NoteId::class)
  @OptIn(ExperimentalSerializationApi::class)
  object SerializationAdapter : KSerializer<NoteId> {
    override val descriptor = PrimitiveSerialDescriptor("NoteId", STRING)

    override fun serialize(encoder: Encoder, value: NoteId) =
      encoder.encodeString(value.value.toString())

    override fun deserialize(decoder: Decoder) =
      NoteId(uuidFrom(decoder.decodeString()))
  }

  companion object {
    fun generate() = NoteId(uuid4())
    fun from(uuidString: String) = NoteId(uuidFrom(uuidString))
  }
}
