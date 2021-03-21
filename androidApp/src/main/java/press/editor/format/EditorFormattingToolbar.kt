package press.editor.format

import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import androidx.core.view.plusAssign
import androidx.core.view.setPadding
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.wysiwyg.formatting.BlockQuoteSyntaxApplier
import me.saket.wysiwyg.formatting.EmphasisSyntaxApplier
import me.saket.wysiwyg.formatting.HeadingSyntaxApplier
import me.saket.wysiwyg.formatting.InlineCodeSyntaxApplier
import me.saket.wysiwyg.formatting.MarkdownSyntaxApplier
import me.saket.wysiwyg.formatting.ReplaceTextWith
import me.saket.wysiwyg.formatting.StrikethroughSyntaxApplier
import me.saket.wysiwyg.formatting.StrongEmphasisSyntaxApplier
import me.saket.wysiwyg.formatting.TextSelection
import me.saket.wysiwyg.formatting.from
import press.editor.copyWysiwygSpansTo
import press.extensions.updatePadding
import press.theme.themeAware
import press.widgets.DividerDrawable
import press.widgets.PressBorderlessImageButton
import press.widgets.dp

class EditorFormattingToolbar(
  private val editorEditText: EditText
) : HorizontalScrollView(editorEditText.context) {

  private val actionListView = LinearLayout(context).apply {
    orientation = HORIZONTAL
    gravity = Gravity.CENTER
  }

  init {
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
      FormatAction(
        iconRes = R.drawable.ic_twotone_undo_24,
        label = context.strings().editor.formattingtoolbar_undo,
        onClick = { editorEditText.onTextContextMenuItem(android.R.id.undo) }
      )
    )
    actionListView += createButton(
      FormatAction(
        iconRes = R.drawable.ic_twotone_redo_24,
        label = context.strings().editor.formattingtoolbar_redo,
        onClick = { editorEditText.onTextContextMenuItem(android.R.id.redo) }
      )
    )
    actionListView += createSeparator()
    actionListView += createButton(
      FormatAction(
        iconRes = R.drawable.ic_format_heading_24,
        label = context.strings().editor.formattingtoolbar_heading,
        onClick = { applyMarkdownSyntax(HeadingSyntaxApplier) }
      )
    )
    actionListView += createButton(
      FormatAction(
        iconRes = R.drawable.ic_twotone_format_strong_emphasis_24,
        label = context.strings().editor.formattingtoolbar_strong_emphasis,
        onClick = { applyMarkdownSyntax(StrongEmphasisSyntaxApplier) }
      )
    )
    actionListView += createButton(
      FormatAction(
        iconRes = R.drawable.ic_twotone_format_emphasis_24,
        label = context.strings().editor.formattingtoolbar_emphasis,
        onClick = { applyMarkdownSyntax(EmphasisSyntaxApplier) }
      )
    )
    actionListView += createButton(
      FormatAction(
        iconRes = R.drawable.ic_twotone_format_strikethrough_24,
        label = context.strings().editor.formattingtoolbar_strikethrough,
        onClick = { applyMarkdownSyntax(StrikethroughSyntaxApplier) }
      )
    )
    actionListView += createSeparator()
    actionListView += createButton(
      FormatAction(
        iconRes = R.drawable.ic_format_inline_code,
        label = context.strings().editor.formattingtoolbar_inline_code,
        onClick = { applyMarkdownSyntax(InlineCodeSyntaxApplier) }
      )
    )
    actionListView += createButton(
      FormatAction(
        iconRes = R.drawable.ic_baseline_format_quote_24,
        label = context.strings().editor.formattingtoolbar_blockquote,
        onClick = { applyMarkdownSyntax(BlockQuoteSyntaxApplier) }
      )
    )
  }

  private fun createButton(action: FormatAction): View {
    return PressBorderlessImageButton(context).also {
      it.contentDescription = action.label
      it.tooltipText = action.label
      it.setPadding(dp(12))
      it.setOnClickListener { action.onClick(it) }
      it.setImageResource(action.iconRes)
    }
  }

  private fun createSeparator(): View {
    return View(context).also {
      it.layoutParams = LayoutParams(dp(1), MATCH_PARENT)
      it.themeAware { palette ->
        it.background = DividerDrawable(palette.separator)
      }
    }
  }

  private fun applyMarkdownSyntax(applier: MarkdownSyntaxApplier) {
    val selection = TextSelection.from(editorEditText)
    if (selection.isNotEmpty)
      updateText(applier.apply(editorEditText.text, selection))
  }

  private fun updateText(text: ReplaceTextWith) {
    // Retain all spans. Without this, all styling are lost until the next parsing of
    // markdown is complete. This results in a flicker everytime a formatting button is clicked.
    editorEditText.text = SpannableStringBuilder(text.replacement).apply {
      editorEditText.text.copyWysiwygSpansTo(this)
    }

    //editorEditText.text.replace(0, editorEditText.text.length, newText)

    text.newSelection?.let {
      editorEditText.setSelection(it.start, it.end)
    }
  }
}

private data class FormatAction(
  val iconRes: Int,
  val label: String,
  val onClick: (View) -> Unit
)
