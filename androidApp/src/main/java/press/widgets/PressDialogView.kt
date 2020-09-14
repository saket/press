package press.widgets

import android.content.Context
import android.graphics.drawable.PaintDrawable
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import com.squareup.contour.ContourLayout
import me.saket.press.shared.theme.TextStyles.mainTitle
import me.saket.press.shared.theme.TextStyles.smallTitle
import me.saket.press.shared.theme.TextView
import press.extensions.padding
import press.extensions.textColor
import press.theme.themeAware
import press.theme.themed

/**
 * Rounded corners and theme colors, because [AlertDialog] isn't very customizable.
 * Essentially copies dialogs from [https://cash.app]'s Android app.
 */
class PressDialogView private constructor(context: Context) : ContourLayout(context) {
  private val titleView = themed(TextView(context, mainTitle)).apply {
    gravity = CENTER_HORIZONTAL
    themeAware { textColor = it.textColorPrimary }
    layoutBy(
        x = matchParentX(marginLeft = 20.dip, marginRight = 20.dip),
        y = topTo { parent.top() + 20.ydip }
    )
  }

  private val messageView = themed(TextView(context, smallTitle)).apply {
    gravity = CENTER_HORIZONTAL
    themeAware { textColor = it.textColorPrimary }
    applyLayout(
        x = matchParentX(marginLeft = 20.dip, marginRight = 20.dip),
        y = topTo {
          if (titleView.isVisible) {
            titleView.bottom() + 16.ydip
          } else {
            parent.top() + 20.ydip
          }
        }
    )
  }

  private val negativeButtonView = themed(PressBorderlessButton(context)).apply {
    padding = dp(16)
    themeAware { textColor = it.textColorPrimary }
    layoutBy(
        x = leftTo { parent.left() }.rightTo { parent.centerX() },
        y = topTo { buttonsTopSeparator.bottom() }
    )
  }

  private val positiveButtonView = themed(PressBorderlessButton(context)).apply {
    padding = dp(16)
    isSingleLine = true
    themeAware { textColor = it.accentColor }
    applyLayout(
        x = leftTo { if (negativeButtonView.isVisible) parent.centerX() else parent.left() }
            .rightTo { parent.right() },
        y = topTo { negativeButtonView.top() }
    )
  }

  private val buttonsTopSeparator = View(context).apply {
    themeAware { setBackgroundColor(it.separator) }
    applyLayout(
        x = matchParentX(),
        y = topTo { messageView.bottom() + 20.ydip }.heightOf { 1.ydip }
    )
  }

  @Suppress("unused")
  private val buttonsMidSeparator = View(context).apply {
    themeAware { setBackgroundColor(it.separator) }
    applyLayout(
        x = centerHorizontallyTo { parent.centerX() }.widthOf { 1.xdip },
        y = topTo { buttonsTopSeparator.bottom() }.bottomTo { parent.bottom() }
    )
  }

  init {
    clipToOutline = true
    elevation = 20f

    themeAware {
      background = PaintDrawable(it.window.backgroundColor).apply {
        setCornerRadius(8f.dip)
      }
    }
    contourHeightOf { positiveButtonView.bottom() }
  }

  companion object {
    fun show(
      context: Context,
      title: CharSequence? = null,
      message: CharSequence,
      negativeButton: String? = null,
      positiveButton: String,
      positiveOnClick: () -> Unit,
      dismissOnOutsideTap: Boolean = true
    ) {
      val dialogView = PressDialogView(context).apply {
        titleView.text = title
        titleView.isVisible = !title.isNullOrBlank()
        messageView.text = message
        negativeButtonView.text = negativeButton
        negativeButtonView.isVisible = !negativeButton.isNullOrBlank()
        buttonsMidSeparator.isVisible = negativeButtonView.isVisible
        positiveButtonView.text = positiveButton
      }

      val dialog = AlertDialog.Builder(context)
          .setView(FrameLayout(context).also {
            it.elevation = dialogView.elevation
            it.addView(dialogView)
            dialogView.updateLayoutParams<MarginLayoutParams> {
              setMargins(context.dp(40))
            }
          })
          .show()
          .apply {
            if (!dismissOnOutsideTap) {
              setCanceledOnTouchOutside(false)
              setOnKeyListener { _, keyCode, _ ->
                keyCode == KeyEvent.KEYCODE_BACK
              }
            }
            window!!.setBackgroundDrawable(null)
          }

      dialogView.apply {
        negativeButtonView.setOnClickListener {
          dialog.dismiss()
        }
        positiveButtonView.setOnClickListener {
          positiveOnClick()
          dialog.dismiss()
        }
      }
    }
  }
}
