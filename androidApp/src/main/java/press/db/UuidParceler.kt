package press.db

import android.os.Parcel
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import kotlinx.android.parcel.Parceler

object UuidParceler : Parceler<Uuid> {
  override fun create(parcel: Parcel) = uuidFrom(parcel.readString()!!)
  override fun Uuid.write(parcel: Parcel, flags: Int) = parcel.writeString(toString())
}
