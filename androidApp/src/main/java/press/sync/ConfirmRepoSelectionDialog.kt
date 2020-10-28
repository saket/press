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
      message = Html.fromHtml(strings.confirm_repo_message.format(repo.ownerAndName)),
      negativeButton = strings.confirm_repo_cancel_button,
      positiveButton = strings.confirm_repo_confirm_button,
      positiveOnClick = onConfirm
    )
  }
}
