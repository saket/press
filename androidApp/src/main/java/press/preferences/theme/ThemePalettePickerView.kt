package press.preferences.theme

import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import me.saket.press.shared.listen
import me.saket.press.shared.listenPreChanges
import me.saket.press.shared.preferences.Setting
import me.saket.press.shared.theme.TextStyles.mainTitle
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextView
import me.saket.press.shared.theme.ThemePalette
import press.extensions.textColor
import press.preferences.theme.PalettePreviewAdapter.VH
import press.theme.appTheme
import press.theme.themeAware
import press.widgets.SpacingItemDecoration

class ThemePalettePickerView(
  context: Context,
  title: String,
  palettes: List<ThemePalette>,
  private val setting: Setting<ThemePalette>
) : ContourLayout(context) {

  private val titleView = TextView(context, mainTitle).apply {
    text = title
    themeAware { textColor = it.textColorPrimary }
  }

  private val subtitleView = TextView(context, smallBody).apply {
    setting.listen().takeUntil(detaches()).subscribe { (selectedPalette) ->
      text = selectedPalette!!.name
    }
    themeAware { textColor = it.textColorSecondary }
  }

  private val paletteListView = RecyclerView(context).apply {
    clipToPadding = false
    layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
    scrollBarStyle = SCROLLBARS_OUTSIDE_OVERLAY
    addItemDecoration(SpacingItemDecoration(spacing = 8.dip, orientation = HORIZONTAL))
    updatePaddingRelative(start = 20.dip, end = 20.dip, top = 16.dip, bottom = 20.dip)
  }

  private val themeTransition = CircularRevealTransition()
  private val paletteAdapter = PalettePreviewAdapter(palettes, onClick = ::changeThemeTo)

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
    paletteListView.adapter = paletteAdapter
    contourHeightOf { paletteListView.bottom() }

    // Theme change transition is decoupled from theme clicks to support theme changes
    // made outside this View (for example when "Theme mode" preference is changed).
    appTheme().listenPreChanges().takeUntil(detaches()).subscribe { palette ->
      if (palette in paletteAdapter.palettes) {
        val previewView = paletteListView.children
          .filterIsInstance<ThemePalettePreviewView>()
          .firstOrNull { it.palette == palette }

        if (previewView != null) {
          themeTransition.beginTransition(anchor = previewView)
        }
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
      }
    }
  }

  private fun changeThemeTo(palette: ThemePalette) {
    if (!themeTransition.isOngoing) {
      setting.set(palette)
    }
  }
}

private class PalettePreviewAdapter(
  val palettes: List<ThemePalette>,
  private val onClick: (ThemePalette) -> Unit
) : RecyclerView.Adapter<VH>() {

  override fun getItemCount(): Int {
    return palettes.size
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    return VH(ThemePalettePreviewView(parent.context))
  }

  override fun onBindViewHolder(holder: VH, position: Int) {
    val palette = palettes[position]
    holder.view.render(palette)
    holder.view.setOnClickListener { onClick(palette) }
  }

  private class VH(val view: ThemePalettePreviewView) : RecyclerView.ViewHolder(view)
}
