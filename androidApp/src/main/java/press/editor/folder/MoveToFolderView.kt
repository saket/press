package press.editor.folder

import android.content.Context
import android.graphics.Color.BLACK
import android.util.AttributeSet
import android.view.Gravity.CENTER
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import me.saket.press.R
import me.saket.press.shared.editor.folder.MoveToFolderPresenter
import me.saket.press.shared.localization.strings
import me.saket.wysiwyg.style.withOpacity
import press.navigation.NotPullCollapsible
import press.navigation.navigator
import press.widgets.PressDialogView

class MoveToFolderView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  presenterFactory: MoveToFolderPresenter.Factory
) : FrameLayout(context), NotPullCollapsible {

  private val dialogView = PressDialogView(context)
  private val contentView = ContentView(context)

  init {
    id = R.id.movetofolder_view

    addView(dialogView)
    dialogView.updateLayoutParams<LayoutParams> { gravity = CENTER }

    setBackgroundColor(BLACK.withOpacity(0.35f))
    setOnClickListener {
      navigator().goBack()
    }

    dialogView.render(
      title = context.strings().movetofolder.movetofolder_title,
      negativeButton = context.strings().movetofolder.movetofolder_cancel,
      positiveButton = context.strings().movetofolder.movetofolder_submit,
      negativeOnClick = { navigator().goBack() }
    )
    dialogView.replaceMessageWith(contentView)
  }
}

private class ContentView(context: Context) : ContourLayout(context) {

}
