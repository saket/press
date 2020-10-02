package press.sync

import android.content.Context
import android.text.TextUtils.TruncateAt.END
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ImageView.ScaleType.CENTER_INSIDE
import androidx.core.view.updatePadding
import com.squareup.contour.ContourLayout
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.sync.SyncPreferencesUiModel.SyncEnabled
import me.saket.press.shared.sync.git.GitHost.GITHUB
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextStyles.smallTitle
import me.saket.press.shared.theme.TextView
import press.extensions.createRippleDrawable
import press.extensions.textColor
import press.theme.themeAware
import press.widgets.PressButton

class SyncEnabledView(context: Context) : ContourLayout(context) {
  private val itemView = ItemView(context)

  val disableButton = PressButton(context, smallBody).apply {
    text = context.strings().sync.disable_sync_button
    themeAware { textColor = it.textColorPrimary }
  }

  init {
    itemView.layoutBy(
        x = matchParentX(),
        y = topTo { parent.top() }
    )
    disableButton.layoutBy(
        x = leftTo { parent.left() + itemView.paddingLeft },
        y = topTo { itemView.bottom() + 20.ydip }
    )

    contourHeightOf { disableButton.bottom() }
  }

  @Suppress("DEPRECATION")
  fun render(model: SyncEnabled) {
    itemView.render(model)
  }

  private class ItemView(context: Context) : ContourLayout(context) {
    private val iconView = ImageView(context).apply {
      scaleType = CENTER_INSIDE
      themeAware { setColorFilter(it.textColorPrimary) }
    }

    private val nameView = TextView(context, smallTitle).apply {
      maxLines = 1
      ellipsize = END
      themeAware { textColor = it.textColorPrimary }
    }

    private val statusView = TextView(context, smallBody).apply {
      themeAware { textColor = it.textColorSecondary }
    }

    private val optionsButton = ImageButton(context).apply {
      setImageResource(R.drawable.ic_more_horiz_24)
      themeAware {
        setColorFilter(it.textColorSecondary)
        background = createRippleDrawable(it, borderless = true)
      }
    }

    init {
      iconView.layoutBy(
          x = leftTo { parent.left() }.widthOf { 32.xdip },
          y = topTo { parent.top() + 6.ydip }.heightOf { 32.ydip }
      )
      nameView.layoutBy(
          x = leftTo { iconView.right() + 20.xdip }.rightTo { optionsButton.left() - 20.xdip },
          y = topTo { parent.top() }
      )
      statusView.layoutBy(
          x = matchXTo(nameView),
          y = topTo { nameView.bottom() + 4.ydip }
      )
      optionsButton.layoutBy(
          x = rightTo { parent.right() }.widthOf { 40.xdip },
          y = topTo { parent.top() }.heightOf { 40.ydip }
      )

      updatePadding(left = 20.dip, top = 20.dip, right = 20.dip, bottom = 20.dip)
      contourHeightWrapContent()

      themeAware {
        background = createRippleDrawable(it)
      }
      setOnClickListener {  }
      optionsButton.setOnClickListener {  }
    }

    fun render(model: SyncEnabled) {
      iconView.setImageResource(
          when (model.gitHost) {
            GITHUB -> R.drawable.ic_github_32dp
          }
      )
      nameView.text = model.remoteName
      statusView.text = model.status
      contentDescription = context.strings().sync.cd_sync_repository_options.format(model.remoteName)

      // ContourLayout tries to make invalidations less eager,
      // but it seems to not play nice with ViewFlipper.
      // TODO: Fix this bug in ContourLayout
      requestLayout()
    }
  }
}
