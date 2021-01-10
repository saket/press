package press.widgets

import android.content.Context
import android.graphics.drawable.PaintDrawable
import android.view.Gravity.CENTER
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import com.squareup.contour.ContourLayout
import me.saket.press.shared.theme.TextStyles.mainTitle
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextView
import press.extensions.padding
import press.extensions.textColor
import press.theme.themeAware

/**
 * Rounded corners and theme colors, because [AlertDialog] isn't very customizable.
 * Essentially copies dialogs from [https://cash.app]'s Android app.
 */
class PressDialogView(context: Context) : ContourLayout(context) {
  private val titleView = TextView(context, mainTitle).apply {
    gravity = CENTER_HORIZONTAL
    themeAware { textColor = it.textColorPrimary }
    layoutBy(
      x = matchParentX(marginLeft = 20.dip, marginRight = 20.dip),
      y = topTo { parent.top() + 20.ydip }
    )
  }

  private val messageView = TextView(context, smallBody).apply {
    gravity = CENTER_HORIZONTAL
    themeAware { textColor = it.textColorPrimary }
    updatePaddingRelative(start = 20.dip, end = 20.dip)
  }

  private val contentView = FrameLayout(context).apply {
    addView(messageView)
  }

  private val negativeButtonView = PressBorderlessButton(context, smallBody).apply {
    padding = dp(16)
    themeAware { textColor = it.textColorPrimary }
    layoutBy(
      x = leftTo { parent.left() }.rightTo { parent.centerX() },
      y = topTo { buttonsTopSeparator.bottom() }
    )
  }

  val positiveButtonView = PressBorderlessButton(context, smallBody).apply {
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
      y = topTo { contentView.bottom() + 20.ydip }.heightOf { 1.ydip }
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
    elevation = dp(20f)

    contentView.layoutBy(
      x = matchParentX(),
      y = topTo {
        if (titleView.isVisible) {
          titleView.bottom() + 16.ydip
        } else {
          parent.top() + 20.ydip
        }
      }
    )

    themeAware {
      background = PaintDrawable(it.window.backgroundColor).apply {
        setCornerRadius(8f.dip)
      }
    }

    contourWidthOf { available -> minOf(300.xdip, available - 60.xdip) }
    contourHeightOf { positiveButtonView.bottom() }
  }

  fun render(
    title: CharSequence? = null,
    message: CharSequence? = null,
    negativeButton: String? = null,
    positiveButton: String,
    negativeOnClick: () -> Unit,
    positiveOnClick: () -> Unit = {}
  ) {
    titleView.text = title
    titleView.isVisible = !title.isNullOrBlank()
    messageView.text = message

    negativeButtonView.text = negativeButton
    negativeButtonView.isVisible = !negativeButton.isNullOrBlank()
    negativeButtonView.setOnClickListener { negativeOnClick() }

    buttonsMidSeparator.isVisible = negativeButtonView.isVisible

    positiveButtonView.text = positiveButton
    positiveButtonView.setOnClickListener { positiveOnClick() }
  }

  fun replaceMessageWith(vararg views: View) {
    contentView.removeAllViews()
    for (view in views) {
      contentView.addView(view)
    }
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
      val dialogView = PressDialogView(context)
      val dialog = AlertDialog.Builder(context)
        .setView(
          FrameLayout(context).also {
            it.elevation = dialogView.elevation
            it.addView(dialogView)
            dialogView.updateLayoutParams<FrameLayout.LayoutParams> { gravity = CENTER }
          }
        )
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

      dialogView.render(
        title = title,
        message = message,
        negativeButton = negativeButton,
        positiveButton = positiveButton,
        negativeOnClick = {
          dialog.dismiss()
        },
        positiveOnClick = {
          positiveOnClick()
          dialog.dismiss()
        }
      )
    }
  }
}
