package press.editor.format

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM
import android.view.Gravity.CENTER
import android.view.HapticFeedbackConstants
import android.view.HapticFeedbackConstants.LONG_PRESS
import android.view.View
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.inSpans
import androidx.core.view.plusAssign
import androidx.core.view.setPadding
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.theme.MarkdownPalette
import me.saket.press.shared.theme.TextStyles.smallTitle
import me.saket.press.shared.theme.ThemePalette
import me.saket.wysiwyg.formatting.BlockQuoteSyntaxApplier
import me.saket.wysiwyg.formatting.EmphasisSyntaxApplier
import me.saket.wysiwyg.formatting.HeadingSyntaxApplier
import me.saket.wysiwyg.formatting.InlineCodeSyntaxApplier
import me.saket.wysiwyg.formatting.MarkdownSyntaxApplier
import me.saket.wysiwyg.formatting.ParagraphBounds
import me.saket.wysiwyg.formatting.StrikethroughSyntaxApplier
import me.saket.wysiwyg.formatting.StrongEmphasisSyntaxApplier
import me.saket.wysiwyg.formatting.TextSelection
import me.saket.wysiwyg.formatting.from
import me.saket.wysiwyg.spans.MonospaceTypefaceSpan
import me.saket.wysiwyg.style.withOpacity
import press.editor.MarkdownEditText
import press.extensions.createBorderlessRippleDrawable
import press.extensions.createRippleDrawable
import press.extensions.showKeyboard
import press.extensions.updatePadding
import press.theme.themeAware
import press.widgets.PressBorderlessImageButton
import press.widgets.PressButton
import press.widgets.dp

class EditorFormattingToolbar(
  private val editorEditText: MarkdownEditText
) : HorizontalScrollView(editorEditText.context) {

  private val actionListView = LinearLayout(context).apply {
    orientation = HORIZONTAL
    gravity = CENTER
  }

  init {
    isHorizontalScrollBarEnabled = false
    isFillViewport = true
    clipToPadding = false
    updatePadding(horizontal = dp(12))
    themeAware {
      // A background is important so that the button ripples are drawn over it instead of
      // over the editor in the background, and thus clipped to this layout's height.
      setBackgroundColor(it.window.elevatedBackgroundColor)
    }
    addView(actionListView)

    actionListView += createButton(
      FormatActionIcon(
        iconRes = R.drawable.ic_twotone_undo_24,
        label = context.strings().editor.formattingtoolbar_undo,
        onClick = { editorEditText.onTextContextMenuItem(android.R.id.undo) }
      )
    )
    actionListView += createButton(
      FormatActionIcon(
        iconRes = R.drawable.ic_twotone_redo_24,
        label = context.strings().editor.formattingtoolbar_redo,
        onClick = { editorEditText.onTextContextMenuItem(android.R.id.redo) }
      )
    )
    actionListView += createDivider()
    actionListView += createButton(
      FormatActionIcon(
        iconRes = R.drawable.ic_format_heading_24,
        label = context.strings().editor.formattingtoolbar_heading,
        onClick = { applyMarkdownSyntax(HeadingSyntaxApplier) }
      )
    )
    actionListView += createButton(
      FormatActionIcon(
        iconRes = R.drawable.ic_twotone_format_strong_emphasis_24,
        label = context.strings().editor.formattingtoolbar_strong_emphasis,
        onClick = { applyMarkdownSyntax(StrongEmphasisSyntaxApplier) }
      )
    )
    actionListView += createButton(
      FormatActionIcon(
        iconRes = R.drawable.ic_twotone_format_emphasis_24,
        label = context.strings().editor.formattingtoolbar_emphasis,
        onClick = { applyMarkdownSyntax(EmphasisSyntaxApplier) }
      )
    )
    actionListView += createButton(
      FormatActionIcon(
        iconRes = R.drawable.ic_twotone_format_strikethrough_24,
        label = context.strings().editor.formattingtoolbar_strikethrough,
        onClick = { applyMarkdownSyntax(StrikethroughSyntaxApplier) }
      )
    )
    actionListView += createDivider()
    actionListView += createButton(
      FormatActionText(
        label = { palette ->
          buildSpannedString {
            color(palette.accentColor) { append('`') }
            inSpans(MonospaceTypefaceSpan {}) {
              append(context.strings().editor.formattingtoolbar_inline_code)
            }
            color(palette.accentColor) { append('`') }
          }
        },
        onClick = { applyMarkdownSyntax(InlineCodeSyntaxApplier) }
      )
    )
    actionListView += createDivider()
    actionListView += createButton(
      FormatActionText(
        label = { palette ->
          buildSpannedString {
            color(palette.markdown.blockQuoteTextColor) { append("> ") }
            append(context.strings().editor.formattingtoolbar_blockquote)
          }
        },
        onClick = { applyMarkdownSyntax(BlockQuoteSyntaxApplier) }
      )
    )
  }

  private fun createButton(action: FormatAction): View {
    val button = when (action) {
      is FormatActionText -> {
        PressButton(context, smallTitle).also {
          it.elevation = 0f
          it.stateListAnimator = null
          it.gravity = CENTER
          it.compoundDrawablePadding = dp(4)
          it.layoutParams = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
          it.updatePadding(horizontal = dp(12), vertical = dp(4))
          it.themeAware { palette ->
            it.text = action.label(palette)
          }
        }
      }
      is FormatActionIcon -> {
        PressBorderlessImageButton(context).also {
          it.contentDescription = action.label
          it.tooltipText = action.label
          it.layoutParams = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
          it.setPadding(dp(12))
          it.setImageResource(action.iconRes)
        }
      }
    }

    button.setOnClickListener {
      button.performHapticFeedback(LONG_PRESS)
      action.onClick(button)
    }
    button.themeAware { palette ->
      button.background = createBorderlessRippleDrawable(
        palette = palette,
        background = palette.buttonPressed
      )
    }
    return button
  }

  private fun createDivider(): View {
    return View(context).also {
      it.layoutParams = LayoutParams(dp(1), MATCH_PARENT)
      it.themeAware { palette ->
        it.background = GradientDrawable(
          TOP_BOTTOM,
          intArrayOf(palette.separator.withOpacity(0f), palette.separator)
        )
      }
    }
  }

  private fun applyMarkdownSyntax(applier: MarkdownSyntaxApplier) {
    val selection = TextSelection.from(editorEditText)
    if (selection != null) {
      val replacement = applier.apply(editorEditText.text, selection)
      editorEditText.setTextWithoutBustingUndoHistory(replacement.replacement, replacement.newSelection)

    } else {
      editorEditText.let {
        // If the cursor isn't visible, show the keyboard at the end
        // of the first paragraph (this will usually be the heading).
        val firstParagraph = ParagraphBounds.find(editorEditText.text, TextSelection.cursor(0))
        it.setSelection(firstParagraph.endExclusive)
        it.requestFocus()
        it.showKeyboard()
      }
    }
  }

}

private sealed class FormatAction {
  abstract val onClick: (View) -> Unit
}

private data class FormatActionIcon(
  val iconRes: Int,
  val label: String,
  override val onClick: (View) -> Unit
) : FormatAction()

private data class FormatActionText(
  val label: (palette: ThemePalette) -> CharSequence,
  override val onClick: (View) -> Unit
) : FormatAction()
