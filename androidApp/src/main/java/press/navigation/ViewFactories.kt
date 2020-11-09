package press.navigation

import android.content.Context
import android.view.View
import com.squareup.inject.inflation.ViewFactory
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.home.HomeScreenKey
import me.saket.press.shared.ui.ScreenKey
import press.editor.EditorView
import press.home.HomeView
import javax.inject.Inject

// Inflation-inject generates a map of view-class-names to their view-factories.
class ViewFactories @Inject constructor(
  private val factories: Map<String, @JvmSuppressWildcards ViewFactory>
) {

  @Suppress("UNCHECKED_CAST")
  fun <T : View> createView(context: Context, screen: ScreenKey): T {
    val name = when (screen) {
      is HomeScreenKey -> HomeView::class
      is EditorScreenKey -> EditorView::class
      else -> error("Missing mapping for $screen")
    }.qualifiedName

    val factory = factories[name] ?: error("No ViewFactory found for $name. Have factories for: ${factories.keys}")
    return factory.create(context, null) as T
  }
}
