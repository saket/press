package press.preferences.theme

import android.annotation.SuppressLint
import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.press.shared.listen
import me.saket.press.shared.preferences.Setting
import me.saket.press.shared.theme.DisplayUnits
import me.saket.press.shared.theme.TextStyles.mainTitle
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextStyles.tinyBody
import me.saket.press.shared.theme.TextView
import me.saket.press.shared.theme.ThemePalette
import me.saket.press.shared.theme.palettes.wysiwygStyle
import me.saket.wysiwyg.Wysiwyg
import press.extensions.textColor
import press.theme.appTheme
import press.theme.themeAware
import press.widgets.Drawables
import press.widgets.SpacingItemDecoration
import press.widgets.dp
import press.widgets.withRipple

class ThemePalettePickerView(context: Context) : ContourLayout(context) {
  private val themeTransition = CircularRevealTransition()
  private val titleView = TextView(context, mainTitle)
  private val subtitleView = TextView(context, smallBody)

  private val paletteListView = RecyclerView(context).apply {
    clipToPadding = false
    layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
    scrollBarStyle = SCROLLBARS_OUTSIDE_OVERLAY
    addItemDecoration(SpacingItemDecoration(spacing = 8.dip, orientation = HORIZONTAL))
    updatePaddingRelative(start = 20.dip, end = 20.dip, top = 16.dip, bottom = 20.dip)
  }

  init {
    titleView.layoutBy(
      x = matchParentX(marginLeft = 20.dip, marginRight = 20.dip),
      y = topTo { parent.top() + 16.dip }
    )
    subtitleView.layoutBy(
      x = matchXTo(titleView),
      y = topTo { titleView.bottom() }
    )
    paletteListView.layoutBy(
      x = matchParentX(),
      y = topTo { subtitleView.bottom() }
    )
    contourHeightOf { paletteListView.bottom() }

    themeAware {
      titleView.textColor = it.textColorPrimary
      subtitleView.textColor = it.textColorSecondary
    }
  }

  fun render(
    title: String,
    palettes: List<ThemePalette>,
    setting: Setting<ThemePalette>
  ) {
    check(paletteListView.adapter == null)

    titleView.text = title
    setting.listen()
      .takeUntil(detaches())
      .observeOn(mainThread())
      .subscribe { (selectedPalette) ->
        subtitleView.text = selectedPalette!!.name
      }

    class VH(val view: ThemePalettePreviewView) : RecyclerView.ViewHolder(view)
    paletteListView.adapter = object : RecyclerView.Adapter<VH>() {
      override fun getItemCount() = palettes.size
      override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ThemePalettePreviewView(context))

      override fun onBindViewHolder(holder: VH, position: Int) {
        val palette = palettes[position]
        holder.view.render(palette)
        holder.view.setOnClickListener {
          changeThemeTo(setting, palette, anchorView = holder.view)
        }
      }
    }
  }

  private fun changeThemeTo(setting: Setting<ThemePalette>, palette: ThemePalette, anchorView: View) {
    if (appTheme().palette == palette || themeTransition.isOngoing) {
      return
    }

    themeTransition.beginTransition(anchor = anchorView)
    appTheme().change(palette)
    setting.set(palette)

    anchorView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
  }
}

private class ThemePalettePreviewView(context: Context) : ContourLayout(context) {
  private val previewTextView = TextView(context, tinyBody).apply {
    maxLines = 4
    updatePaddingRelative(start = dp(24), top = dp(20), end = -dp(20))
  }

  init {
    elevation = 2f.dip

    previewTextView.layoutBy(
      // The text extends beyond this View's right bounds.
      // This design was copied from Bear. Looks pretty cool.
      x = leftTo { parent.left() }.rightTo { parent.right() + 50.xdip },
      y = topTo { parent.top() }
    )
    contourWidthOf { 200.xdip }
    contourHeightOf { previewTextView.bottom() }
  }

  @SuppressLint("SetTextI18n")
  fun render(palette: ThemePalette) {
    val markdown = """
        ### ${palette.name}
        To live is to *risk it all*, otherwise you're just an [inert chunk](...) of randomly assembled \
        molecules drifting wherever the universe blows you.
        """.trimIndent().replace("\\\n", "")

    previewTextView.let {
      it.textColor = palette.textColorPrimary
      it.text = Wysiwyg.highlightImmediately(markdown, palette.wysiwygStyle(DisplayUnits(context)))
    }

    background = Drawables
      .roundedRect(palette.window.elevatedBackgroundColor, cornerRadius = dp(4f))
      .withRipple(rippleColor = palette.accentColor)
  }
}
