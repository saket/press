package press.editor.folder

import android.content.Context
import android.graphics.Color.BLACK
import android.util.AttributeSet
import android.view.Gravity.CENTER
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import androidx.core.view.updateLayoutParams
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.press.R
import me.saket.press.shared.editor.folder.CreateFolderEvent.FolderPathTextChanged
import me.saket.press.shared.editor.folder.CreateFolderEvent.SubmitClicked
import me.saket.press.shared.editor.folder.CreateFolderModel
import me.saket.press.shared.editor.folder.CreateFolderPresenter
import me.saket.press.shared.editor.folder.CreateFolderScreenKey
import me.saket.press.shared.localization.strings
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.applyStyle
import me.saket.press.shared.ui.models
import me.saket.wysiwyg.style.withOpacity
import press.extensions.doOnEditorAction
import press.extensions.doOnTextChange
import press.extensions.resizeAndBind
import press.extensions.setTextAndCursor
import press.extensions.showKeyboard
import press.extensions.textColor
import press.navigation.NotPullCollapsible
import press.navigation.navigator
import press.navigation.screenKey
import press.theme.themePalette
import press.widgets.MaterialTextInputLayout
import press.widgets.PressDialogView

class CreateFolderView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  private val presenterFactory: CreateFolderPresenter.Factory
) : FrameLayout(context), NotPullCollapsible {

  private val dialogView = PressDialogView(context)
  private val contentView = ContentView(context)

  init {
    id = R.id.createfolder_view

    addView(dialogView)
    dialogView.updateLayoutParams<LayoutParams> { gravity = CENTER }

    setBackgroundColor(BLACK.withOpacity(0.35f))
    setOnClickListener {
      navigator().goBack()
    }

    dialogView.render(
      title = context.strings().createfolder.createfolder_title,
      negativeButton = context.strings().createfolder.createfolder_cancel,
      positiveButton = context.strings().createfolder.createfolder_submit,
      negativeOnClick = { navigator().goBack() }
    )
    dialogView.replaceMessageWith(contentView)

    contentView.textField.editText.run {
      setTextAndCursor(screenKey<CreateFolderScreenKey>().preFilledFolderPath)
      doOnEditorAction(IME_ACTION_DONE) {
        dialogView.positiveButtonView.performClick()
      }
    }
    post {
      contentView.textField.editText.showKeyboard()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val presenter = presenterFactory.create(
      args = CreateFolderPresenter.Args(
        screenKey = screenKey(),
        navigator = navigator()
      )
    )
    dialogView.positiveButtonView.setOnClickListener {
      presenter.dispatch(SubmitClicked)
    }
    contentView.textField.editText.doOnTextChange {
      presenter.dispatch(FolderPathTextChanged(it.toString()))
    }

    presenter.models()
      .observeOn(mainThread())
      .takeUntil(detaches())
      .subscribe(::render)
  }

  private fun render(model: CreateFolderModel) {
    contentView.run {
      textField.error = model.errorMessage
      textField.isErrorEnabled = model.errorMessage != null

      suggestionsContainer.resizeAndBind(
        size = 3,
        viewCreator = { FolderSuggestionRowView(context) },
        viewBinder = { index, view ->
          view.render(
            model = model.suggestions.getOrNull(index),
            showDivider = index < 2,
            onClick = {
              textField.editText.setTextAndCursor(model.suggestions[index].name.text)
            }
          )
        }
      )

      // Not sure why the text field loses focus when an error is shown.
      if (!textField.hasFocus()) {
        textField.requestFocus()
      }
    }
  }
}

private class ContentView(context: Context) : ContourLayout(context) {
  val textField = MaterialTextInputLayout(context).apply {
    editText.applyStyle(smallBody)
    editText.id = R.id.createfolder_folder_name
    editText.isSingleLine = true
    editText.imeOptions = editText.imeOptions or IME_ACTION_DONE
    editText.textColor = themePalette().textColorPrimary
    hint = context.strings().createfolder.createfolder_name_hint
  }

  val suggestionsContainer = LinearLayout(context).apply {
    orientation = VERTICAL
  }

  init {
    textField.layoutBy(
      x = matchParentX(marginLeft = 20.dip, marginRight = 20.dip),
      y = topTo { parent.top() }
    )
    suggestionsContainer.layoutBy(
      x = matchParentX(),
      y = topTo { textField.bottom() }
    )
    contourHeightOf { suggestionsContainer.bottom() }

    // Seed views.
    suggestionsContainer.let {
      it.addView(FolderSuggestionRowView(context))
      it.addView(FolderSuggestionRowView(context))
      it.addView(FolderSuggestionRowView(context))
    }
  }
}
