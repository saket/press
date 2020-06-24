package me.saket.press.shared.db

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import com.squareup.sqldelight.ColumnAdapter
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind.STRING
import kotlinx.serialization.Serializer

// Inline class would have been nice if Kotlin multiplatform supported it.
data class NoteId(val value: Uuid) {
  object SqlAdapter : ColumnAdapter<NoteId, String> {
    override fun decode(databaseValue: String) = NoteId(uuidFrom(databaseValue))
    override fun encode(value: NoteId) = value.value.toString()
  }

  @Serializer(forClass = NoteId::class)
  object SerializationAdapter : KSerializer<NoteId> {
    override val descriptor = PrimitiveDescriptor("NoteId", STRING)

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
