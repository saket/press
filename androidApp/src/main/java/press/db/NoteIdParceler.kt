package press.db

import android.os.Parcel
import com.benasher44.uuid.uuidFrom
import kotlinx.android.parcel.Parceler
import me.saket.press.shared.db.NoteId

object NoteIdParceler : Parceler<NoteId> {
  override fun create(parcel: Parcel) = NoteId(uuidFrom(parcel.readString()!!))
  override fun NoteId.write(parcel: Parcel, flags: Int) = parcel.writeString(value.toString())
}
