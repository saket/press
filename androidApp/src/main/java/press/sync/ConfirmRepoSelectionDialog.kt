package press.sync

import android.content.Context
import androidx.appcompat.app.AlertDialog
import me.saket.press.shared.localization.strings
import me.saket.press.shared.sync.git.service.GitRepositoryInfo

class ConfirmRepoSelectionDialog(
  context: Context,
  repo: GitRepositoryInfo,
  onConfirm: () -> Unit
) : AlertDialog(context) {

  init {
    val strings = context.strings().sync
    setMessage(strings.confirmSelectionMessage.format(repo.name))
    setButton(BUTTON_POSITIVE, strings.confirmSelectionConfirmButton) { _, _ -> onConfirm() }
    setButton(BUTTON_NEGATIVE, strings.confirmSelectionCancelButton) { _, _ -> }
  }
}
