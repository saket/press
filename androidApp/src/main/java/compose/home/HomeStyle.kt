package compose.home

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import compose.theme.AppStyle
import compose.theme.Styleable
import compose.theme.TextAppearance
import me.saket.compose.shared.theme.ThemePalette

class HomeStyle(
  private val palette: ThemePalette,
  val noteRow: NoteRow = NoteRow(palette),
  val newNoteFab: Fab = Fab(palette)
) : AppStyle(palette) {

  class NoteRow(
    palette: ThemePalette,
    val title: TextAppearance = TextAppearance(color = palette.headingColor),
    val body: TextAppearance = TextAppearance(color = palette.textColorSecondary)
  )

  class Fab(
    palette: ThemePalette,
    private val background: Int = palette.fabColor
  ) : Styleable<FloatingActionButton> {

    override fun style(view: FloatingActionButton) {
      view.backgroundTintList = ColorStateList.valueOf(background)
      view.setColorFilter(ColorUtils.blendARGB(background, Color.BLACK, 0.65f))
    }
  }
}