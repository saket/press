package press.sync

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.widget.AppCompatButton
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import me.saket.press.shared.localization.strings
import press.theme.themeAware
import press.theme.themed
import press.widgets.PressToolbar

class SyncPreferencesView @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted private val onDismiss: () -> Unit
) : ContourLayout(context) {

  private val toolbar = themed(PressToolbar(context)).apply {
    title = context.strings().syncPreferences.title
    setNavigationOnClickListener { onDismiss() }
    applyLayout(
        x = matchParentX(),
        y = topTo { parent.top() }
    )
  }

  private val authorizeButton = AppCompatButton(context).apply {
    text = "Log into GitHub"
    setOnClickListener {
      context.startActivity(GitHostAuthActivity.intent(context))
    }
    applyLayout(
        x = centerHorizontallyTo { parent.centerX() },
        y = centerVerticallyTo { parent.centerY() }
    )
  }

  init {
    themeAware {
      background = ColorDrawable(it.window.backgroundColor)
    }
  }

  @AssistedInject.Factory
  interface Factory {
    fun create(context: Context, onDismiss: () -> Unit): SyncPreferencesView
  }
}
