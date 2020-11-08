package press.navigation

import android.os.Parcelable
import android.view.View
import kotlin.reflect.KClass

abstract class ScreenKey(val viewClass: KClass<out View>) : Parcelable
