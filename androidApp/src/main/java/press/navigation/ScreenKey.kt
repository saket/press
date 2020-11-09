package press.navigation

import android.os.Parcelable
import android.view.View
import kotlin.reflect.KClass

/** @param viewClass Used for creating the View through Dagger. */
abstract class ScreenKey(val viewClass: KClass<out View>) : Parcelable
