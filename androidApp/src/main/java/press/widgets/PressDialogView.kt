package press.widgets

import android.content.Context
import android.graphics.Color.TRANSPARENT
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.PaintDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import com.squareup.contour.ContourLayout
import me.saket.press.shared.theme.TextStyles
import me.saket.press.shared.theme.applyStyle
import press.extensions.TextView
import press.theme.themeAware
import press.theme.themed
import press.extensions.padding
import press.extensions.textColor

/**
 * Rounded corners and theme colors, because [AlertDialog] isn't very customizable.
 * Essentially copies dialogs from [https://cash.app]'s Android app.
 */
class PressDialogView private constructor(context: Context) : ContourLayout(context) {
  private val messageView = themed(TextView(context, TextStyles.Secondary)).apply {
    gravity = Gravity.CENTER
    themeAware { textColor = it.textColorPrimary }
    applyLayout(
        x = matchParentX(marginLeft = 20.dip, marginRight = 20.dip),
        y = topTo { parent.top() + 20.ydip }
    )
  }

  private val negativeButtonView = themed(PressBorderlessButton(context)).apply {
    padding = dp(16)
    themeAware { textColor = it.textColorPrimary }
    applyLayout(
        x = leftTo { parent.left() }.rightTo { parent.centerX() },
        y = topTo { buttonsTopSeparator.bottom() }
    )
  }

  private val positiveButtonView = themed(PressBorderlessButton(context)).apply {
    padding = dp(16)
    isSingleLine = true
    themeAware { textColor = it.accentColor }
    applyLayout(
        x = leftTo { parent.centerX() }.rightTo { parent.right() },
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
      message: CharSequence,
      negativeButton: String,
      positiveOnClick: () -> Unit,
      positiveButton: String
    ) {
      val dialogView = PressDialogView(context)
      val dialog = AlertDialog.Builder(context)
          .setView(FrameLayout(context).also {
            it.elevation = dialogView.elevation
            it.addView(dialogView)
            dialogView.updateLayoutParams<MarginLayoutParams> {
              setMargins(context.dp(40))
            }
          })
          .show()
          .apply { window!!.setBackgroundDrawable(ColorDrawable(TRANSPARENT)) }

      dialogView.apply {
        messageView.text = message
        negativeButtonView.text = negativeButton
        positiveButtonView.text = positiveButton
        negativeButtonView.setOnClickListener { dialog.dismiss() }
        positiveButtonView.setOnClickListener {
          positiveOnClick()
          dialog.dismiss()
        }
      }
    }
  }
}
