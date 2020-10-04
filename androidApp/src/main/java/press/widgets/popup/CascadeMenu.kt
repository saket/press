package press.widgets.popup

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.BLACK
import android.graphics.Color.TRANSPARENT
import android.graphics.Color.WHITE
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.RippleDrawable
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionSet
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.OnMenuItemClickListener
import android.view.SubMenu
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.iterator
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import androidx.core.widget.PopupWindowCompat

// TODO
//  - window margins.
@SuppressLint("RestrictedApi")
class CascadeMenu(
  private val context: Context,
  fixedWidthInDp: Int = 200,
  private val gravity: Int = Gravity.START or Gravity.CENTER_VERTICAL
) : PopupWindow(context, null) {
  val menu: Menu = MenuBuilder(context)
  var onMenuItemClickListener: OnMenuItemClickListener? = null
  private val fixedWidth = fixedWidthInDp.dip

  init {
    elevation = 16f.dip   // @dimen/floating_window_z
    isFocusable = true    // Dismiss on outside touch.
    isOutsideTouchable = true

    setBackgroundDrawable(null)   // Remove PopupWindow's default frame around the content.
    PopupWindowCompat.setOverlapAnchor(this, true)

    enterTransition = createEnterTransition()
    exitTransition = createExitTransition()
  }

  class Styler(
    val background: (Drawable) -> Drawable = { it },
    val menuTitle: (TextView, MenuItem) -> Unit = { _, _ -> },
    val menuItem: (TextView, MenuItem) -> Unit = { _, _ -> }
  )

  fun show(anchor: View, styler: Styler) {
    contentView = HeightAnimatableViewFlipper(context).apply {
      background = createBackground(styler)
      clipToOutline = true

      val onClick = { item: MenuItem ->
        if (item.subMenu != null) {
          showView(createMenuView(item.subMenu, styler, onClick = {
            check(it.subMenu == null) { "todo: support nested menus" }
            onMenuItemClickListener?.onMenuItemClick(it)
            dismiss()
          }))

        } else {
          onMenuItemClickListener?.onMenuItemClick(item)
          dismiss()
        }
      }
      showView(createMenuView(menu, styler, onClick))
    }

    if (false) {
      PopupMenu(context, anchor).also {
        it.menu.add("Open")
        it.menu.addSubMenu("Remove").also { sub ->
          sub.add("Confirm remove")
          sub.add("Wait no")
        }
        it.menu.add("Logs")
        it.show()
      }
    } else {
      showAsDropDown(anchor, 0, 0, gravity)
    }
  }

  private fun createMenuView(
    menu: Menu,
    styler: Styler,
    onClick: (MenuItem) -> Unit
  ): View {
    return LinearLayout(context).apply {
      orientation = VERTICAL
      layoutParams = LayoutParams(fixedWidth, WRAP_CONTENT)

      if (menu is SubMenu) {
        // Apart from the parent container, each sub-menu must have also
        // have a background to avoid leaking the outgoing menu behind.
        background = createBackground(styler)

        addView(TextView(context).also {
          it.textSize = 14f
          it.text = menu.item.title
          it.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
          it.updatePadding(left = 16.dip, right = 16.dip, top = 16.dip, bottom = 8.dip)
          styler.menuTitle(it, menu.item)
        })
      }

      for (item in menu) {
        addView(TextView(context).also {
          it.textSize = 16f
          it.text = item.title
          it.background = createRippleDrawable(Color.GRAY)
          it.setPadding(16.dip)
          styler.menuItem(it, item)
          it.setOnClickListener { onClick(item) }
        }, MATCH_PARENT, WRAP_CONTENT)
      }
    }
  }

  private fun createBackground(styler: Styler): Drawable {
    return styler.background(PaintDrawable(WHITE).apply { setCornerRadius(20f.dip) })
  }

  // Copies android's @transition/popup_window_enter
  private fun createEnterTransition(): Transition {
    return TransitionSet().apply {
      ordering = TransitionSet.ORDERING_TOGETHER
      addTransition(EpicenterTranslateClipReveal().also { it.duration = 250 })
      addTransition(Fade().also { it.duration = 100; })
    }
  }

  // Copies android's @transition/popup_window_exit
  private fun createExitTransition(): Transition {
    return Fade().also { it.duration = 300 }
  }

  private val Float.dip: Float
    get() {
      val metrics = context.resources.displayMetrics
      return TypedValue.applyDimension(COMPLEX_UNIT_DIP, this, metrics)
    }

  private val Int.dip: Int
    get() {
      val metrics = context.resources.displayMetrics
      return TypedValue.applyDimension(COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
    }

  private fun createRippleDrawable(color: Int): Drawable {
    val shape = PaintDrawable(TRANSPARENT)
    val mask = PaintDrawable(BLACK)
    return RippleDrawable(ColorStateList.valueOf(color), shape, mask)
  }
}
