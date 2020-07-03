package press.sync

import android.content.Context
import android.text.Html
import me.saket.press.shared.localization.strings
import me.saket.press.shared.sync.git.service.GitRepositoryInfo
import press.widgets.PressDialogView

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
