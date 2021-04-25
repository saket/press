package press.editor.folder

import android.content.Context
import android.graphics.Color.BLACK
import android.util.AttributeSet
import android.view.Gravity.CENTER
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import io.reactivex.android.schedulers.AndroidSchedulers
import me.saket.press.R
import me.saket.press.shared.editor.folder.MoveToFolderModel
import me.saket.press.shared.editor.folder.MoveToFolderPresenter
import me.saket.press.shared.localization.strings
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.applyStyle
import me.saket.press.shared.ui.models
import me.saket.wysiwyg.style.withOpacity
import press.extensions.setTextAndCursor
import press.extensions.showKeyboard
import press.extensions.textColor
import press.navigation.NotPullCollapsible
import press.navigation.navigator
import press.navigation.screenKey
import press.theme.themePalette
import press.widgets.MaterialTextInputLayout
import press.widgets.PressDialogView

class MoveToFolderView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  private val presenterFactory: MoveToFolderPresenter.Factory
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

    post {
      contentView.textField.editText.showKeyboard()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val presenter = presenterFactory.create(
      args = MoveToFolderPresenter.Args(screenKey = screenKey())
    )

    presenter.models()
      .observeOn(AndroidSchedulers.mainThread())
      .takeUntil(detaches())
      .subscribe(::render)
  }

  private fun render(model: MoveToFolderModel) {
    contentView.textField.let {
      it.editText.setTextAndCursor(model.folderPath)
      it.error = model.errorMessage

      // Not sure why the text field loses focus when an error is shown.
      if (!it.hasFocus()) {
        it.requestFocus()
      }
    }
  }
}

private class ContentView(context: Context) : ContourLayout(context) {
  val textField = MaterialTextInputLayout(context).apply {
    editText.applyStyle(smallBody)
    editText.id = R.id.movetofolder_folder_name
    editText.isSingleLine = true
    editText.imeOptions = editText.imeOptions or IME_ACTION_DONE
    hint = context.strings().movetofolder.movetofolder_folder_name_hint
    isHelperTextEnabled = true
    editText.textColor = themePalette().textColorPrimary
  }

  init {
    textField.layoutBy(
      x = matchParentX(marginLeft = 20.dip, marginRight = 20.dip),
      y = topTo { parent.top() }
    )
    contourHeightWrapContent()
  }
}
