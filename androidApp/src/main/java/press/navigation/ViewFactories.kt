package press.navigation

import android.view.View
import com.squareup.inject.inflation.ViewFactory
import javax.inject.Inject
import kotlin.reflect.KClass

// Inflation-inject generates a map of view-class-names to their view-factories.
class ViewFactories @Inject constructor(
  private val factories: Map<String, @JvmSuppressWildcards ViewFactory>
) {

  fun factoryFor(klass: KClass<out View>): ViewFactory {
    val name = klass.java.canonicalName
    return factories[name] ?: error("No ViewFactory found for $name. Have factories for: ${factories.keys}")
  }
}
