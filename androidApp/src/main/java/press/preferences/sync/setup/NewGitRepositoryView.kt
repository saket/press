package press.preferences.sync.setup

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity.CENTER
import android.view.inputmethod.EditorInfo.IME_ACTION_GO
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryEvent.NameTextChanged
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryEvent.SubmitClicked
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryPresenter
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryPresenter.Args
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryScreenKey
import me.saket.press.shared.preferences.sync.setup.NewGitRepositoryUiModel
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.applyStyle
import me.saket.press.shared.ui.models
import me.saket.wysiwyg.style.withOpacity
import press.extensions.doOnEditorAction
import press.extensions.doOnTextChange
import press.extensions.setTextAndCursor
import press.extensions.showKeyboard
import press.extensions.textColor
import press.navigation.NotPullCollapsible
import press.navigation.navigator
import press.navigation.screenKey
import press.theme.themePalette
import press.widgets.MaterialTextInputLayout
import press.widgets.PressDialogView

class NewGitRepositoryView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  presenterFactory: NewGitRepositoryPresenter.Factory
) : FrameLayout(context), NotPullCollapsible {

  private val dialogView = PressDialogView(context)
  private val contentView = ContentView(context)

  private val presenter = presenterFactory.create(
    Args(
      screenKey = screenKey(),
      navigator = navigator()
    )
  )

  init {
    id = R.id.newgitrepo_view

    addView(dialogView)
    dialogView.updateLayoutParams<LayoutParams> { gravity = CENTER }

    setBackgroundColor(Color.BLACK.withOpacity(0.35f))
    setOnClickListener {
      navigator().goBack()
    }

    dialogView.render(
      title = context.strings().sync.newgitrepo_title,
      negativeButton = context.strings().sync.newgitrepo_cancel,
      positiveButton = context.strings().sync.newgitrepo_submit,
      negativeOnClick = {
        navigator().goBack()
      }
    )
    dialogView.replaceMessageWith(contentView)

    contentView.textField.editText.run {
      setTextAndCursor(screenKey<NewGitRepositoryScreenKey>().preFilledRepoName)
      doOnEditorAction(IME_ACTION_GO) {
        dialogView.positiveButtonView.performClick()
      }
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    contentView.textField.editText.apply {
      post { showKeyboard() }
    }
    dialogView.positiveButtonView.setOnClickListener {
      presenter.dispatch(SubmitClicked)
    }
    contentView.textField.editText.doOnTextChange {
      presenter.dispatch(NameTextChanged(it.toString()))
    }

    presenter.models()
      .observeOn(mainThread())
      .takeUntil(detaches())
      .subscribe(::render)
  }

  private fun render(model: NewGitRepositoryUiModel) {
    contentView.textField.apply {
      helperText = model.repoUrlPreview
      isHelperTextEnabled = true  // TextInputLayout hides space for helper text if it's null.
      error = model.errorMessage
    }
    dialogView.positiveButtonView.isEnabled = model.submitEnabled

    TransitionManager.beginDelayedTransition(this, Fade().setDuration(150))
    contentView.apply {
      textField.isInvisible = model.isLoading
      loadingView.isVisible = model.isLoading
    }

    // Not sure why the text field loses focus when an error is shown.
    if (!contentView.textField.hasFocus()) {
      contentView.textField.requestFocus()
    }
  }
}

private class ContentView(context: Context) : ContourLayout(context) {
  val textField = MaterialTextInputLayout(context).apply {
    editText.applyStyle(smallBody)
    editText.id = R.id.newgitrepo_repo_name
    editText.isSingleLine = true
    editText.imeOptions = editText.imeOptions or IME_ACTION_GO
    hint = context.strings().sync.newgitrepo_name_hint
    isHelperTextEnabled = true
    editText.textColor = themePalette().textColorPrimary
  }

  val loadingView = ProgressBar(context)

  init {
    textField.layoutBy(
      x = matchParentX(marginLeft = 20.dip, marginRight = 20.dip),
      y = topTo { parent.top() }
    )
    loadingView.layoutBy(
      x = matchParentX(),
      y = centerVerticallyTo { textField.centerY() }
    )
    contourHeightWrapContent()

    loadingView.isVisible = false
  }
}
