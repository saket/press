package press.preferences.theme

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.squareup.contour.ContourLayout
import me.saket.press.shared.theme.DisplayUnits
import me.saket.press.shared.theme.TextStyles.mainTitle
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextStyles.tinyBody
import me.saket.press.shared.theme.TextView
import me.saket.press.shared.theme.ThemePalette
import me.saket.press.shared.theme.wysiwygStyle
import me.saket.wysiwyg.Wysiwyg
import press.extensions.rippleDrawable
import press.extensions.textColor
import press.theme.themeAware
import press.widgets.Drawables
import press.widgets.SpacingItemDecoration
import press.widgets.dp
import press.widgets.withRipple

class ThemePalettePickerView(context: Context) : ContourLayout(context) {
  private val titleView = TextView(context, mainTitle)
  private val subtitleView = TextView(context, smallBody)

  private val paletteListView = RecyclerView(context).apply {
    clipToPadding = false
    layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
    scrollBarStyle = SCROLLBARS_OUTSIDE_OVERLAY
    addItemDecoration(SpacingItemDecoration(spacing = 8.dip, orientation = HORIZONTAL))
    updatePaddingRelative(start = 12.dip, end = 12.dip, top = 16.dip, bottom = 20.dip)
  }

  init {
    titleView.layoutBy(
      x = matchParentX(marginLeft = 16.dip, marginRight = 16.dip),
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
      background = rippleDrawable(it)
    }
  }

  fun render(
    title: String,
    palettes: List<ThemePalette>,
    selected: ThemePalette,
    onSelect: (ThemePalette) -> Unit
  ) {
    titleView.text = title
    subtitleView.text = selected.name

    class VH(val view: ThemePaletteRowView) : RecyclerView.ViewHolder(view)
    paletteListView.adapter = object : RecyclerView.Adapter<VH>() {
      override fun getItemCount() = palettes.size
      override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ThemePaletteRowView(context, onSelect))
      override fun onBindViewHolder(holder: VH, position: Int) = holder.view.render(palettes[position])
    }
  }
}

private class ThemePaletteRowView(
  context: Context,
  private val onSelect: (ThemePalette) -> Unit
) : ContourLayout(context) {

  private val sampleTextView = TextView(context, tinyBody).apply {
    maxLines = 4
    updatePaddingRelative(start = dp(24), top = dp(20), end = -dp(20))
  }

  init {
    elevation = 2f.dip

    sampleTextView.layoutBy(
      // The text extends beyond this View's right bounds.
      // This design was copied from Bear. Looks pretty cool.
      x = leftTo { parent.left() }.rightTo { parent.right() + 50.xdip },
      y = topTo { parent.top() }
    )
    contourWidthOf { 200.xdip }
    contourHeightOf { sampleTextView.bottom() }
  }

  @SuppressLint("SetTextI18n")
  fun render(palette: ThemePalette) {
    val markdown = """
        ### ${palette.name}
        To live is to *risk it all*, otherwise you're just an [inert chunk](...) of randomly assembled \
        molecules drifting wherever the universe blows you.
        """.trimIndent().replace("\\\n", "")

    sampleTextView.let {
      it.textColor = palette.textColorPrimary
      it.text = Wysiwyg.highlightImmediately(markdown, palette.wysiwygStyle(DisplayUnits(context)))
    }

    background = Drawables
      .roundedRect(palette.window.elevatedBackgroundColor, cornerRadius = dp(4f))
      .withRipple(rippleColor = palette.accentColor)

    setOnClickListener {
      onSelect(palette)
    }
  }
}
