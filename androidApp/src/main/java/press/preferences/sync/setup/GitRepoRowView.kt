package press.preferences.sync.setup

import android.content.Context
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import io.reactivex.rxkotlin.Observables.combineLatest
import io.reactivex.subjects.BehaviorSubject
import me.saket.press.shared.listenRx
import me.saket.press.shared.preferences.sync.setup.HighlightedText
import me.saket.press.shared.preferences.sync.setup.RepoUiModel
import me.saket.press.shared.theme.TextStyles.mainTitle
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextView
import me.saket.press.shared.theme.palettes.ThemePalette
import press.extensions.rippleDrawable
import press.extensions.textColor
import press.theme.appTheme
import press.theme.themeAware

class GitRepoRowView(context: Context) : ContourLayout(context) {
  private val ownerView = TextView(context, smallBody).apply {
    themeAware { textColor = it.textColorSecondary }
    applyLayout(
      x = matchParentX(marginLeft = 22.dip, marginRight = 22.dip),
      y = topTo { parent.top() + 16.ydip }
    )
  }

  private val nameView = TextView(context, mainTitle).apply {
    themeAware { textColor = it.textColorPrimary }
    applyLayout(
      x = matchParentX(marginLeft = 22.dip, marginRight = 22.dip),
      y = topTo { ownerView.bottom() }
    )
  }

  private val dividerView = View(context).apply {
    themeAware { setBackgroundColor(it.separator) }
    applyLayout(
      x = matchParentX(),
      y = topTo { nameView.bottom() + 16.ydip }.heightOf { 1.ydip }
    )
  }

  init {
    contourHeightOf { dividerView.bottom() }

    themeAware {
      // RV item animations nicer if the items don't leak through each other.
      background = rippleDrawable(it, background = it.window.backgroundColor)
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    combineLatest(models, appTheme().listenRx())
      .takeUntil(detaches())
      .subscribe { (model, palette) -> render(model, palette) }
  }

  private val models = BehaviorSubject.create<RepoUiModel>()
  fun render(model: RepoUiModel) {
    models.onNext(model)
  }

  private fun render(model: RepoUiModel, palette: ThemePalette) {
    val highlightSpan = ForegroundColorSpan(palette.accentColor)
    ownerView.text = model.owner.withSpan(highlightSpan)
    nameView.text = model.name.withSpan(highlightSpan)
  }

  private fun HighlightedText.withSpan(span: Any): CharSequence {
    return when (val it = highlight) {
      null -> return text
      else -> SpannableString(text).apply {
        setSpan(span, it.first, it.last, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
      }
    }
  }
}
