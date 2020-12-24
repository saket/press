package press.sync

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo.IME_ACTION_GO
import android.widget.EditText
import android.widget.ProgressBar
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.jakewharton.rxbinding3.view.detaches
import com.jakewharton.rxbinding3.widget.editorActions
import com.squareup.contour.ContourLayout
import com.squareup.contour.SizeMode.AtMost
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.sync.git.NewGitRepositoryEvent.NameTextChanged
import me.saket.press.shared.sync.git.NewGitRepositoryEvent.SubmitClicked
import me.saket.press.shared.sync.git.NewGitRepositoryPresenter
import me.saket.press.shared.sync.git.NewGitRepositoryPresenter.Args
import me.saket.press.shared.sync.git.NewGitRepositoryUiModel
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.applyStyle
import me.saket.press.shared.ui.subscribe
import me.saket.press.shared.ui.uiUpdates
import press.extensions.doOnEditorAction
import press.extensions.doOnTextChange
import press.extensions.showKeyboard
import press.extensions.textColor
import press.extensions.withOpacity
import press.navigation.NotPullCollapsible
import press.navigation.navigator
import press.navigation.screenKey
import press.theme.themeAware
import press.widgets.MaterialTextInputLayout
import press.widgets.PressDialogView

class NewGitRepositoryView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  presenterFactory: NewGitRepositoryPresenter.Factory
) : ContourLayout(context), NotPullCollapsible {

  private val dialogView = PressDialogView(context)
  private val contentView = ContentView(context)
  private val dialogPanTransition = ChangeBounds()
    .addTarget(dialogView)
    .setDuration(200)
    .setInterpolator(FastOutSlowInInterpolator())
    .excludeChildren(dialogView, true)

  private val presenter = presenterFactory.create(
    Args(
      screenKey = screenKey(),
      navigator = navigator()
    )
  )

  init {
    id = R.id.newgitrepo_view
    dialogView.layoutBy(
      x = centerHorizontallyTo { parent.centerX() }.widthOf(AtMost) { 300.xdip },
      y = centerVerticallyTo { parent.centerY() }
    )
    contourWidthMatchParent()
    contourHeightMatchParent()

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
      },
      positiveOnClick = {
        presenter.dispatch(SubmitClicked)
      }
    )
    dialogView.replaceMessageWith(contentView)

    contentView.textField.editText.doOnEditorAction(IME_ACTION_GO) {
      dialogView.positiveButtonView.performClick()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    contentView.textField.editText.apply {
      post { showKeyboard() }

      doOnTextChange {
        presenter.dispatch(NameTextChanged(it.toString()))
      }
    }

    presenter.uiUpdates()
      .observeOn(mainThread())
      .takeUntil(detaches())
      .subscribe(::render)
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    if (changed) {
      TransitionManager.beginDelayedTransition(this, dialogPanTransition)
    }
    super.onLayout(changed, l, t, r, b)
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
    themeAware {
      editText.textColor = it.textColorPrimary
    }
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
