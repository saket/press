package press.editor.folder

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.squareup.contour.ContourLayout
import me.saket.press.shared.editor.folder.FolderSuggestionModel
import me.saket.press.shared.theme.TextStyles.smallBody
import press.extensions.updatePadding
import press.theme.themePalette
import press.widgets.PressBorderlessButton
import press.widgets.dp
import press.widgets.withSpan

class FolderSuggestionRowView(context: Context) : ContourLayout(context) {
  private val dividerView = View(context).apply {
    setBackgroundColor(themePalette().divider)
  }

  private val nameView = PressBorderlessButton(context, smallBody).apply {
    gravity = Gravity.START
    updatePadding(horizontal = dp(36), vertical = dp(12))
  }

  init {
    nameView.layoutBy(
      x = matchParentX(),
      y = topTo { parent.top() }
    )
    dividerView.layoutBy(
      x = matchParentX(),
      y = topTo { nameView.bottom() }.heightOf { 1.ydip }
    )
    contourHeightOf { dividerView.bottom() }
  }

  fun render(model: FolderSuggestionModel?, showDivider: Boolean, onClick: () -> Unit) {
    dividerView.isInvisible = !showDivider
    nameView.text = model?.name?.withSpan()
    nameView.setOnClickListener { onClick() }

    isInvisible = model == null
    isClickable = model != null
  }
}
