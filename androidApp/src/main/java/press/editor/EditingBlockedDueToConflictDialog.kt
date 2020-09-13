package press.editor

import android.content.Context
import me.saket.press.shared.localization.strings
import press.widgets.PressDialogView

object EditingBlockedDueToConflictDialog {
  fun show(context: Context, onDismiss: () -> Unit) {
    PressDialogView.show(
        context = context,
        title = context.strings().sync.conflicted_note_explanation_dialog_title,
        message = context.strings().sync.conflicted_note_explanation_dialog_message,
        positiveButton = context.strings().sync.conflicted_note_explanation_dialog_button,
        positiveOnClick = onDismiss,
        dismissOnOutsideTap = false
    )
  }
}
