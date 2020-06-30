package press.sync

import android.content.Context
import android.graphics.Color.TRANSPARENT
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import me.saket.press.shared.localization.strings
import me.saket.press.shared.sync.git.service.GitRepositoryInfo
import press.widgets.PressDialogView
import press.widgets.dp
import press.widgets.padding

@Suppress("DEPRECATION")
object ConfirmRepoSelectionDialog {
  fun show(context: Context, repo: GitRepositoryInfo, onConfirm: () -> Unit) {
    val strings = context.strings().sync
    PressDialogView.show(
        context = context,
        message = Html.fromHtml(strings.confirmSelectionMessage.format(repo.name)),
        negativeButton = strings.confirmSelectionCancelButton,
        positiveButton = strings.confirmSelectionConfirmButton,
        positiveOnClick = onConfirm
    )
  }
}
