package press.preferences.sync

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.graphics.Rect
import android.net.Uri
import android.text.TextUtils.TruncateAt.END
import android.view.TouchDelegate
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ImageView.ScaleType.CENTER_INSIDE
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import com.squareup.contour.ContourLayout
import me.saket.cascade.CascadePopupMenu
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.preferences.sync.SyncPreferencesUiModel.SyncEnabled
import me.saket.press.shared.preferences.sync.stats.SyncStatsForNerdsScreenKey
import me.saket.press.shared.syncer.git.GitHost.GITHUB
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextStyles.smallTitle
import me.saket.press.shared.theme.TextView
import press.PressApp
import press.extensions.borderlessRippleDrawable
import press.extensions.rippleDrawable
import press.extensions.textColor
import press.navigation.navigator
import press.theme.pressCascadeStyler
import press.theme.themePalette

class SyncEnabledView(context: Context) : ContourLayout(context) {
  private val itemView = ItemView(context)
  lateinit var onDisableClick: () -> Unit

  init {
    itemView.layoutBy(
      x = matchParentX(),
      y = topTo { parent.top() }
    )
    contourHeightWrapContent()
  }

  fun render(model: SyncEnabled) {
    itemView.render(model)

    val openRepo = { context.startActivity(Intent(ACTION_VIEW, Uri.parse(model.remoteUrl))) }
    itemView.setOnClickListener { openRepo() }

    itemView.optionsButton.setOnClickListener {
      showOptionsMenu(openRepo)
    }
  }

  private fun showOptionsMenu(openRepo: () -> Unit) {
    CascadePopupMenu(context, anchor = itemView.optionsButton, styler = pressCascadeStyler()).apply {
      menu.add(context.strings().sync.open_repository)
        .setIcon(R.drawable.ic_twotone_web_24)
        .setOnMenuItemClickListener {
          openRepo()
          true
        }

      menu.addSubMenu(context.strings().sync.remove_repository)
        .setIcon(R.drawable.ic_twotone_delete_24)
        .setHeaderTitle(context.strings().sync.remove_repository_confirm_question).also {
          val confirmationInRed = buildSpannedString {
            color(PressApp.component.theme().palette.textColorWarning) {
              append(context.strings().sync.remove_repository_confirm)
            }
          }
          it.add(confirmationInRed)
            .setOnMenuItemClickListener {
              onDisableClick()
              true
            }
          it.add(context.strings().sync.remove_repository_cancel)
            .setOnMenuItemClickListener {
              navigateBack()
              true
            }
        }

      menu.add(context.strings().sync.show_sync_stats)
        .setIcon(R.drawable.ic_twotone_bug_report_24)
        .setOnMenuItemClickListener {
          navigator().lfg(SyncStatsForNerdsScreenKey)
          true
        }
      show()
    }
  }

  private class ItemView(context: Context) : ContourLayout(context) {
    private val iconView = ImageView(context).apply {
      scaleType = CENTER_INSIDE
      setColorFilter(themePalette().textColorPrimary)
    }

    val nameView = TextView(context, smallTitle).apply {
      maxLines = 1
      ellipsize = END
      textColor = themePalette().textColorPrimary
    }

    private val statusView = TextView(context, smallBody).apply {
      textColor = themePalette().textColorSecondary
    }

    val optionsButton = ImageButton(context).apply {
      background = borderlessRippleDrawable()
      setImageResource(R.drawable.ic_more_horiz_24)
      setColorFilter(themePalette().textColorSecondary)
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

      background = rippleDrawable()
      updatePadding(left = 20.dip, top = 20.dip, right = 10.dip, bottom = 20.dip)
      contourHeightWrapContent()

      // Avoid accidental taps on parent by increasing the option button's tap area.
      optionsButton.doOnLayout {
        val optionsRect = it.getHitRect()
        optionsRect.inset(-it.top, -it.top)
        touchDelegate = TouchDelegate(optionsRect, it)
      }
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

private fun View.getHitRect(): Rect {
  return Rect().also { getHitRect(it) }
}
